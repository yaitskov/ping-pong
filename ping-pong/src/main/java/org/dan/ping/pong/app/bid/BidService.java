package org.dan.ping.pong.app.bid;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.app.match.MatchService.incompleteMatchStates;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_UID;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

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

    public List<ParticipantState> findEnlisted(Tid tid) {
        return bidDao.findEnlisted(tid);
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
        if (FILLER_LOSER_UID.equals(bid.getUid()) || bid.getState() == target) {
            return;
        }
        log.info("Set bid {} state {}", bid.getUid(), target);
        if (!expected.contains(bid.getState())) {
            throw internalError(
                    "Bid " + bid.getUid() + " state "
                            + bid.getState() + " but expected " + expected);
        }
        bid.setBidState(target);
        bidDao.setBidState(bid.getTid(), bid.getUid(),
                target, expected, clocker.get(), batch);
    }

    public List<UserLink> findByState(TournamentMemState tournament, List<BidState> states) {
        return tournament.getParticipants().values().stream()
                .filter(p -> states.contains(p.getState()))
                .filter(p -> tournament.participantMatches(p.getUid())
                        .anyMatch(m -> incompleteMatchStates.contains(m.getState())))
                .map(ParticipantMemState::toLink)
                .sorted(Comparator.comparing(UserLink::getName))
                .collect(toList());
    }

    public List<UserLink> findWithMatch(TournamentMemState tournament) {
        return tournament.getParticipants().values().stream()
                .filter(p -> tournament.participantMatches(p.getUid())
                        .anyMatch(m -> m.getState() == Over
                                || m.getState() == Game && m.getPlayedSets() > 0))
                .map(ParticipantMemState::toLink)
                .sorted(Comparator.comparing(UserLink::getName))
                .collect(toList());
    }
}
