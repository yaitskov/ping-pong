package org.dan.ping.pong.app.bid;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.match.MatchState.INCOMPLETE_MATCH_STATES;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.tournament.EnlistPolicy.MULTIPLE_CATEGORY_ENLISTMENT;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.castinglots.CastingLotsService;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.sched.ScheduleServiceSelector;
import org.dan.ping.pong.app.tournament.ChildTournamentProvider;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

@Slf4j
public class BidService {
    @Inject
    private BidDao bidDao;

    public void paid(TournamentMemState tournament, Bid bid, DbUpdater batch) {
        setBidState(tournament.getParticipant(bid), Paid, singletonList(Want), batch);
    }

    public void readyToPlay(TournamentMemState tournament, Bid bid, DbUpdater batch) {
        setBidState(tournament.getParticipant(bid), Here, asList(Paid, Want), batch);
    }

    public List<ParticipantState> findEnlisted(TournamentMemState tournament) {
        return tournament.participants()
                .map(p -> ParticipantState
                        .builder()
                        .user(p.toBidLink())
                        .state(p.getBidState())
                        .category(tournament.getCategory(p.getCid()).toLink())
                        .build())
                .collect(toList());
    }

    @Inject
    private Clocker clocker;

    public void setCategory(TournamentMemState tournament, SetCategory setCategory, DbUpdater batch) {
        tournament.checkCategory(setCategory.getTargetCid());
        tournament.checkCategory(setCategory.getExpectedCid());
        final ParticipantMemState par = tournament.getParticipant(setCategory.getBid());
        if (!par.getCid().equals(setCategory.getExpectedCid())) {
            throw badRequest("Category has been changed by someone else");
        }
        if (setCategory.getTargetCid().equals(setCategory.getExpectedCid())) {
            return;
        }
        ensureNoMultipleCategoryEnlistment(tournament, setCategory.getTargetCid(), par);
        switch (tournament.getState()) {
            case Open:
                changeCategoryInRunningTournament(tournament, setCategory, batch);
                break;
            case Draft:
                changeCategoryInDraft(tournament, setCategory.getTargetCid(), batch, par);
                break;
            default:
                throw badRequest("Tournament state not allow category change");
        }
    }

    private void ensureNoMultipleCategoryEnlistment(
            TournamentMemState tournament, Cid targetCid, ParticipantMemState par) {
        final Map<Cid, Bid> cid2Bids = tournament.getUidCid2Bid().get(par.getUid());
        if (cid2Bids.containsKey(targetCid)) {
            throw badRequest(MULTIPLE_CATEGORY_ENLISTMENT);
        }
    }

    public void changeCategoryInDraft(TournamentMemState tournament,
            Cid targetCid, DbUpdater batch, ParticipantMemState par) {
        final Map<Cid, Bid> cidBidMap = tournament
                .getUidCid2Bid().get(par.getUid());

        if (cidBidMap.remove(par.getCid()) == null) {
            throw internalError("no category " + par.getCid());
        }
        cidBidMap.put(targetCid, par.getBid());
        bidDao.setCategory(SetCategory.builder()
                .bid(par.getBid())
                .targetCid(targetCid)
                .expectedCid(par.getCid())
                .tid(tournament.getTid())
                .build(), clocker.get(), batch);
        par.setCid(targetCid);
    }

    private void changeCategoryInRunningTournament(
            TournamentMemState tournament, SetCategory setCategory, DbUpdater batch) {
        final TournamentRules rules = tournament.getRule();
        if (rules.getGroup().isPresent()) { // if groups possible
            final ParticipantMemState par = tournament.getParticipant(setCategory.getBid());
            changeGroup(tournament, ChangeGroupReq
                    .builder()
                    .tid(tournament.getTid())
                    .bid(setCategory.getBid())
                    .expectedGid(par.gid())
                    .targetGid(findBestGroup(tournament,
                            setCategory.getTargetCid(),
                            setCategory.getTargetGid()))
                    .build(),
                    batch);

        } else {
            // if free base play off position or  possible to extend ladder
            // find target group if not specified (pick the smallest one)
            // if no group? => find place in the ladder base
            // from group to play off or from play off to play off
            throw badRequest("Change category in open playoff tournament not implemented");
        }
    }

    private Optional<Gid> findBestGroup(
            TournamentMemState tournament,
            Cid targetCid, Optional<Gid> targetGid) {
        if (targetGid.isPresent()) {
            return targetGid;
        }
        final Map<Gid, Long> gidPopulation = tournament.participants()
                .filter(p -> p.getCid().equals(targetCid))
                .collect(groupingBy(ParticipantMemState::gid, counting()));

        tournament.findGroupsByCategory(targetCid)
                .map(GroupInfo::getGid)
                .forEach(gid -> gidPopulation.putIfAbsent(gid, 0L));

        return gidPopulation.entrySet()
                .stream()
                .sorted(comparingLong(Map.Entry::getValue))
                .findFirst()
                .map(Map.Entry::getKey);
    }

    public void setBidState(TournamentMemState tournament, SetBidState setState, DbUpdater batch) {
        setBidState(tournament.getParticipant(setState.getBid()), setState.getTarget(),
                singletonList(setState.getExpected()), batch);
    }

    public void setBidState(ParticipantMemState bid, BidState target,
            Collection<BidState> expected, DbUpdater batch) {
        if (FILLER_LOSER_BID.equals(bid.getBid()) || bid.state() == target) {
            return;
        }
        log.info("Set bid {} state {}", bid.getBid(), target);
        if (!expected.contains(bid.state())) {
            throw internalError(
                    "Bid " + bid.getBid() + " state "
                            + bid.state() + " but expected " + expected);
        }
        bid.setBidState(target);
        bidDao.setBidState(bid.getTid(), bid.getBid(),
                target, expected, clocker.get(), batch);
    }

    @Inject
    private ChildTournamentProvider childTournamentProvider;

    public List<ParticipantLink> findByState(TournamentMemState tournament, List<BidState> states) {
        return Stream.concat(
                childTournamentProvider.getChildren(tournament)
                        .flatMap(consoleTournament -> findByStateNonRecursive(consoleTournament, states)),
                findByStateNonRecursive(tournament, states))
                .sorted(Comparator.comparing(ParticipantLink::getName))
                .collect(toList());
    }

    public Stream<ParticipantLink> findByStateNonRecursive(TournamentMemState tournament,
            List<BidState> states) {
        return tournament.getParticipants().values().stream()
                .filter(p -> states.contains(p.state()))
                .filter(p -> tournament.participantMatches(p.getBid())
                        .anyMatch(m -> INCOMPLETE_MATCH_STATES.contains(m.getState())))
                .map(ParticipantMemState::toBidLink);
    }

    public List<ParticipantLink> findWithMatch(TournamentMemState tournament) {
        return tournament.getParticipants().values().stream()
                .filter(p -> tournament.participantMatches(p.getBid())
                        .anyMatch(m -> m.getState() == Over
                                || m.playedSets() > 0))
                .map(ParticipantMemState::toBidLink)
                .sorted(Comparator.comparing(ParticipantLink::getName))
                .collect(toList());
    }

    @Inject
    private GroupService groupService;

    @Inject
    private CastingLotsService castingLotsService;

    public void changeGroup(TournamentMemState tournament, ChangeGroupReq req, DbUpdater batch) {
        log.info("Change group {}");

        final ParticipantMemState bid = tournament.getBid(req.getBid());

        final Optional<Gid> opExpectedGid = Optional.of(req.getExpectedGid());
        if (tournament.getState() != Open) {
            throw badRequest("Tournament is not open");
        }
        if (!bid.getGid().equals(opExpectedGid)) {
            throw badRequest("Expected group is different",
                    "gid", bid.getGid());
        }
        final Optional<Gid> opTargetGid = req.getTargetGid();
        if (bid.getGid().equals(opTargetGid)) {
            log.info("Target group is the same with expected");
            return;
        }
        req.getTargetGid().ifPresent(tGid -> groupService.checkGroupComplete(tournament, tGid)
                .ifPresent(matches -> {
                    throw badRequest("target group is complete");
                }));
        final Gid targetGid = req.getTargetGid()
                .orElseGet(() ->  castingLotsService.addGroup(tournament, batch, bid.getCid()));
        groupService.checkGroupComplete(tournament, req.getExpectedGid())
                .ifPresent(matches -> {
                    throw badRequest("source group is complete");
                });

        setGroupForUids(batch, targetGid, req.getTid(), singletonList(bid));
        final GroupInfo targetGroup = tournament.getGroup(targetGid);
        final GroupInfo sourceGroup = tournament.getGroup(req.getExpectedGid());

        if (!targetGroup.getCid().equals(sourceGroup.getCid())) {
            ensureNoMultipleCategoryEnlistment(tournament, targetGroup.getCid(), bid);
            changeCategoryInDraft(tournament, targetGroup.getCid(), batch, bid);
        }

        // cancel matches in the source group
        cancelMatchesOf(tournament, req.getBid(), batch);
        // generate matches in target group
        castingLotsService.addParticipant(req.getBid(), tournament, batch);

        tryCompleteSourceGroup(tournament, req.getExpectedGid(), batch);
    }

    @Inject
    private ScheduleServiceSelector scheduleService;

    @Inject
    private MatchService matchService;

    private void tryCompleteSourceGroup(TournamentMemState tournament, Gid gid, DbUpdater batch) {
        matchService.tryToCompleteGroup(tournament, gid, batch);
        scheduleService.afterMatchComplete(tournament, batch, clocker.get());
    }

    @Inject
    private MatchDao matchDao;

    public void cancelMatchesOf(TournamentMemState tournament, Bid bid, DbUpdater batch) {
        log.info("Cancel matches of {} in group", bid);
        final List<MatchInfo> matchesToBeRemoved = tournament
                .participantMatches(bid)
                .filter(m -> m.getGid().isPresent())
                .collect(toList());

        matchesToBeRemoved.forEach(m -> m.getOpponentBid(bid).ifPresent(oUid -> {
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
        final ParticipantMemState bid = tournament.getParticipant(bidRename.getBid());
        if (!bid.getName().equals(bidRename.getExpectedName())) {
            throw badRequest("user name mismatch",
                    ImmutableMap.of(
                            "expected", bidRename.getExpectedName(),
                            "was", bid.getName()));
        }
        bidDao.renameParticipant(bid.getUid(), bidRename.getNewName(), batch);
        tournament.getUidCid2Bid().get(bid.getUid()).values()
                .stream()
                .map(tournament::getParticipant)
                .forEach(p -> p.setName(bidRename.getNewName()));
    }

    public void setGroupForUids(
            DbUpdater batch, Gid gid, Tid tid, List<ParticipantMemState> orderedBids) {
        orderedBids.forEach(bid -> bid.setGid(Optional.of(gid)));
        bidDao.setGroupForUids(batch, gid, tid, orderedBids);
    }
}
