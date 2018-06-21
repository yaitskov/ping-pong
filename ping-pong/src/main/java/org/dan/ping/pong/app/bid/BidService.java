package org.dan.ping.pong.app.bid;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.app.match.MatchState.INCOMPLETE_MATCH_STATES;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.sched.ScheduleCtx.SCHEDULE_SELECTOR;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_UID;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.castinglots.CastingLotsService;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.sched.ScheduleService;
import org.dan.ping.pong.app.tournament.ChildTournamentProvider;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class BidService {
    public static final List<BidState> WIN_STATES = asList(Win1, Win2, Win3);
    public static final Set<BidState> TERMINAL_RECOVERABLE_STATES = ImmutableSet.of(Win1, Win2, Win3, Lost, Play);

    @Inject
    private BidDao bidDao;

    public void paid(TournamentMemState tournament, Uid uid, DbUpdater batch) {
        setBidState(tournament.getParticipant(uid), Paid, singletonList(Want), batch);
    }

    public void readyToPlay(TournamentMemState tournament, Uid uid, DbUpdater batch) {
        setBidState(tournament.getParticipant(uid), Here, asList(Paid, Want), batch);
    }

    public List<ParticipantState> findEnlisted(TournamentMemState tournament) {
        return tournament.participants()
                .map(p -> ParticipantState
                        .builder()
                        .user(p.toLink())
                        .state(p.getBidState())
                        .category(tournament.getCategory(p.getCid()))
                        .build())
                .collect(toList());
    }

    public DatedParticipantState getParticipantState(Tid tid, Uid uid) {
        return bidDao.getParticipantInfo(tid, uid)
                .orElseThrow(() -> notFound("Participant has not been found"));
    }

    @Inject
    private Clocker clocker;

    public void setCategory(TournamentMemState tournament, SetCategory setCategory, DbUpdater batch) {
        tournament.checkCategory(setCategory.getCid());
        tournament.getParticipant(setCategory.getUid()).setCid(setCategory.getCid());
        bidDao.setCategory(setCategory, clocker.get(), batch);
    }

    public void setBidState(TournamentMemState tournament, SetBidState setState, DbUpdater batch) {
        setBidState(tournament.getParticipant(setState.getUid()), setState.getTarget(),
                singletonList(setState.getExpected()), batch);
    }

    public void setBidState(ParticipantMemState bid, BidState target,
            Collection<BidState> expected, DbUpdater batch) {
        if (FILLER_LOSER_UID.equals(bid.getUid()) || bid.state() == target) {
            return;
        }
        log.info("Set bid {} state {}", bid.getUid(), target);
        if (!expected.contains(bid.state())) {
            throw internalError(
                    "Bid " + bid.getUid() + " state "
                            + bid.state() + " but expected " + expected);
        }
        bid.setBidState(target);
        bidDao.setBidState(bid.getTid(), bid.getUid(),
                target, expected, clocker.get(), batch);
    }

    @Inject
    private ChildTournamentProvider childTournamentProvider;

    public List<UserLink> findByState(TournamentMemState tournament, List<BidState> states) {
        return Stream.concat(
                childTournamentProvider.getChild(tournament)
                        .map(consoleTournament -> findByStateNonRecursive(consoleTournament, states))
                        .orElseGet(Stream::empty),
                findByStateNonRecursive(tournament, states))
                .sorted(Comparator.comparing(UserLink::getName))
                .collect(toList());
    }

    public Stream<UserLink> findByStateNonRecursive(TournamentMemState tournament,
            List<BidState> states) {
        return tournament.getParticipants().values().stream()
                .filter(p -> states.contains(p.state()))
                .filter(p -> tournament.participantMatches(p.getUid())
                        .anyMatch(m -> INCOMPLETE_MATCH_STATES.contains(m.getState())))
                .map(ParticipantMemState::toLink);
    }

    public List<UserLink> findWithMatch(TournamentMemState tournament) {
        return tournament.getParticipants().values().stream()
                .filter(p -> tournament.participantMatches(p.getUid())
                        .anyMatch(m -> m.getState() == Over
                                || m.playedSets() > 0))
                .map(ParticipantMemState::toLink)
                .sorted(Comparator.comparing(UserLink::getName))
                .collect(toList());
    }

    @Inject
    private GroupService groupService;

    @Inject
    private CastingLotsService castingLotsService;

    public void changeGroup(TournamentMemState tournament, ChangeGroupReq req, DbUpdater batch) {
        log.info("Change group {}");

        final ParticipantMemState bid = tournament.getBid(req.getUid());

        final Optional<Integer> opExpectedGid = Optional.of(req.getExpectedGid());
        if (tournament.getState() != Open) {
            throw badRequest("Tournament is not open");
        }
        if (!bid.getGid().equals(opExpectedGid)) {
            throw badRequest("Expected group is different",
                    "gid", bid.getGid());
        }
        final Optional<Integer> opTargetGid = req.getTargetGid();
        if (bid.getGid().equals(opTargetGid)) {
            log.info("Target group is the same with expected");
            return;
        }
        req.getTargetGid().ifPresent(tGid -> groupService.checkGroupComplete(tournament, tGid)
                .ifPresent(matches -> {
                    throw badRequest("target group is complete");
                }));
        final int targetGid = req.getTargetGid()
                .orElseGet(() ->  castingLotsService.addGroup(tournament, batch, bid.getCid()));
        groupService.checkGroupComplete(tournament, req.getExpectedGid())
                .ifPresent(matches -> {
                    throw badRequest("source group is complete");
                });

        bidDao.setGroupForUids(batch, targetGid, req.getTid(), singletonList(bid));
        bid.setGid(Optional.of(targetGid));
        // cancel matches in the source group
        cancelMatchesOf(tournament, req.getUid(), batch);
        // generate matches in target group
        castingLotsService.addParticipant(req.getUid(), tournament, batch);

        tryCompleteSourceGroup(tournament, req.getExpectedGid(), batch);
    }

    @Inject
    @Named(SCHEDULE_SELECTOR)
    private ScheduleService scheduleService;

    @Inject
    private MatchService matchService;

    private void tryCompleteSourceGroup(TournamentMemState tournament, int gid, DbUpdater batch) {
        matchService.tryToCompleteGroup(tournament, gid, batch);
        scheduleService.afterMatchComplete(tournament, batch, clocker.get());
    }

    @Inject
    private MatchDao matchDao;

    @Inject
    private BidService bidService;

    public void cancelMatchesOf(TournamentMemState tournament, Uid uid, DbUpdater batch) {
        log.info("Cancel matches of {} in group", uid);
        final List<MatchInfo> matchesToBeRemoved = tournament
                .participantMatches(uid)
                .filter(m -> m.getGid().isPresent())
                .collect(toList());

        matchesToBeRemoved.forEach(m -> m.getOpponentUid(uid).ifPresent(oUid -> {
            final ParticipantMemState opBid = tournament.getBidOrExpl(oUid);
            setBidState(
                    opBid,
                    matchService.completeGroupMatchBidState(tournament, opBid),
                    singleton(Play),
                    batch);
        }));

        final Set<Mid> midsForRemoval = matchesToBeRemoved.stream()
                .map(MatchInfo::getMid).collect(toSet());

        tournament.setMatches(tournament.getMatches()
                .values()
                .stream()
                .filter(m -> !midsForRemoval.contains(m.getMid()))
                .collect(toMap(MatchInfo::getMid, m -> m)));

        matchDao.deleteByIds(tournament.getTid(), midsForRemoval, batch);
    }

    public void rename(TournamentMemState tournament, DbUpdater batch, BidRename bidRename) {
        final ParticipantMemState bid = tournament.getParticipant(bidRename.getUid());
        if (!bid.getName().equals(bidRename.getExpectedName())) {
            throw badRequest("user name mismatch",
                    ImmutableMap.of(
                            "expected", bidRename.getExpectedName(),
                            "was", bid.getName()));
        }
        bidDao.renameParticipant(bidRename.getUid(), bidRename.getNewName(), batch);
        bid.setName(bidRename.getNewName());
    }
}
