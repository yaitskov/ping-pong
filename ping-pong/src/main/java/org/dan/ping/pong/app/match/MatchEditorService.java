package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidService.TERMINAL_RECOVERABLE_STATES;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.match.MatchState.Auto;
import static org.dan.ping.pong.app.match.MatchState.Draft;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.sched.ScheduleCtx.SCHEDULE_SELECTOR;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.sched.ScheduleService;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class MatchEditorService {
    public static final String DONT_CHECK_HASH = "dont-check-hash";

    private static final Set<MatchState> rescorableMatchState = ImmutableSet.of(Game, Over);
    private static final Set<MatchState> GAME_EXPECTED = singleton(Game);
    private static final Set<MatchState> OVER_EXPECTED = singleton(Over);

    @Inject
    private MatchDao matchDao;

    @Inject
    private GroupService groupService;

    @Inject
    private PlayOffService playOffService;

    @Inject
    private MatchService matchService;

    @Inject
    private BidService bidService;

    @Inject
    private Clocker clocker;

    @Inject
    @Named(SCHEDULE_SELECTOR)
    private ScheduleService scheduleService;

    private List<MatchUid> findEffectedMatches(
            TournamentMemState tournament, MatchInfo mInfo,
            Map<Uid, List<Integer>> newSets) {
        if (!mInfo.getWinnerId().isPresent()) {
            return emptyList();
        }
        List<MatchUid> result;
        if (mInfo.getGid().isPresent()) {
            result = findEffectMatchesByMatchInGroup(tournament, mInfo, newSets);
        } else {
            result = findEffectMatchesByMatchInPlayOff(tournament, mInfo, newSets);
        }
        return result.stream()
                .sorted(Comparator.comparing(MatchUid::getMid)
                        .thenComparing(MatchUid::getUid))
                .distinct() // uids can meet which cause some matches to be processed twice
                .collect(toList());
    }

    private List<MatchUid> findEffectMatchesByMatchInPlayOff(
            TournamentMemState tournament, MatchInfo mInfo,
            Map<Uid, List<Integer>> newSets) {
        final Optional<Uid> newWinner = findNewWinnerUid(tournament, newSets, mInfo);
        if (newWinner.equals(mInfo.getWinnerId())) {
            return emptyList();
        }
        final List<MatchUid> result = new ArrayList<>();
        mInfo.getParticipantIdScore().keySet()
                .forEach(uid -> findPlayOffAffectedMatches(
                        tournament, uid, singletonList(mInfo), result));
        return result.stream()
                .filter(m -> !m.getMid().equals(mInfo.getMid()))
                .collect(toList());
    }

    private Optional<Uid> findNewWinnerUid(TournamentMemState tournament,
            Map<Uid, List<Integer>> newSets, MatchInfo minfo) {
        final MatchValidationRule matchRule = tournament.getRule().getMatch();

        if (minfo.getWinnerId().isPresent()) {
            final Optional<Uid> actualPracticalWinnerUid = matchRule.findWinner(minfo);
            // actual uid always = formal uid if actual one is presented,
            // because walkover can happen if match has no scored sets
            // or presented sets are not enough to find out winner
            if (actualPracticalWinnerUid.isPresent()) {
                // match complete normally (by score)
                return matchRule.findWinnerByScores(newSets);
            } else {
                return minfo.getWinnerId(); // walkover, quit or expel
            }
        } else {
            return matchRule.findWinnerByScores(newSets);
        }
    }

    private List<Uid> findAffectedUidsQuittingGroup(
            TournamentMemState tournament, MatchInfo minfo,
            Map<Uid, List<Integer>> newSets,
            List<MatchInfo> groupMatches) {
        final GroupRules groupRules = tournament.getRule().getGroup().get();

        final List<Uid> currentlyQuittingUids = groupService.findUidsQuittingGroup(
                tournament, groupRules, groupMatches);
        final MatchInfo rescoredMatch = minfo.clone();
        final List<MatchInfo> rescoredGroupMatches = groupMatches.stream()
                .filter(mm -> mm.getMid().equals(minfo.getMid()))
                .collect(toList());
        rescoredGroupMatches.add(rescoredMatch);

        rescoredMatch.setParticipantIdScore(newSets);

        final List<Uid> thenQuittingUids = groupService.findUidsQuittingGroup(
                tournament, groupRules, rescoredGroupMatches);

        return findDifferentPairs(thenQuittingUids, currentlyQuittingUids);
    }

    private List<MatchUid> findEffectMatchesByMatchInGroup(
            TournamentMemState tournament, MatchInfo minfo,
            Map<Uid, List<Integer>> newSets) {
        final List<MatchInfo> groupMatches = minfo.getGid()
                .map(gid -> groupService.findMatchesInGroup(tournament, gid))
                .orElse(emptyList());

        if (!allMatchesInGroupComplete(groupMatches)) {
            return emptyList();
        }

        final List<Uid> movedDisappearedUids = findAffectedUidsQuittingGroup(
                tournament, minfo, newSets, groupMatches);

        final List<MatchInfo> basePlayOffMatches = playOffService
                .findBaseMatches(tournament, minfo.getCid());

        final List<MatchUid> result = new ArrayList<>();
        movedDisappearedUids.forEach(
                uid -> findPlayOffAffectedMatches(
                        tournament, uid, basePlayOffMatches, result));
        return result;
    }

    private void findPlayOffAffectedMatches(TournamentMemState tournament,
            Uid uid, List<MatchInfo> matches, List<MatchUid> result) {
        matches.stream()
                .filter(m -> m.getParticipantIdScore().containsKey(uid))
                .forEach(m -> {
                    result.add(MatchUid.builder().uid(uid).mid(m.getMid()).build());
                    m.getWinnerId().ifPresent(wUid -> {
                        if (wUid.equals(uid)) {
                            m.getWinnerMid().ifPresent(wMid ->
                                    findPlayOffAffectedMatches(tournament, uid,
                                            singletonList(tournament.getMatchById(wMid)), result));
                            m.getLoserMid().ifPresent(lMid ->
                                    findPlayOffAffectedMatches(tournament,
                                            m.getOpponentUid(uid)
                                                    .orElseThrow(() -> internalError("no loser")),
                                            singletonList(tournament.getMatchById(lMid)), result));
                        } else {
                            m.getWinnerMid().ifPresent(wMid ->
                                    findPlayOffAffectedMatches(tournament,
                                            m.getOpponentUid(uid)
                                                    .orElseThrow(() -> internalError("no winner")),
                                            singletonList(tournament.getMatchById(wMid)), result));
                            m.getLoserMid().ifPresent(lMid ->
                                    findPlayOffAffectedMatches(tournament,
                                            uid,
                                            singletonList(tournament.getMatchById(lMid)), result));

                        }
                    });
                    if (m.getState() == Auto) {
                        m.getLoserMid().ifPresent(lMid ->
                                findPlayOffAffectedMatches(tournament,
                                        uid,
                                        singletonList(tournament.getMatchById(lMid)), result));
                    }
                });
    }

    private <T> List<T> findDifferentPairs(List<T> thenQuittingUids, List<T> currentlyQuittingUids) {
        if (thenQuittingUids.size() != currentlyQuittingUids.size()) {
            throw internalError("quit uid list size mismatch");
        }
        final List<T> result = new ArrayList<>();
        for (int i = 0; i < thenQuittingUids.size(); ++i) {
            if (!thenQuittingUids.get(i).equals(currentlyQuittingUids.get(i))) {
                result.add(currentlyQuittingUids.get(i));
            }
        }
        return result;
    }

    private EffectedMatch toEffectedMatch(TournamentMemState tournament, MatchInfo minfo) {
        return EffectedMatch.builder()
                .mid(minfo.getMid())
                .participants(minfo.getParticipantIdScore()
                        .keySet().stream().map(tournament::getBid)
                        .map(ParticipantMemState::toLink).collect(toList()))
                .build();
    }

    private String calculateEffectHash(List<MatchUid> matches) {
        if (matches.isEmpty()) {
            return "";
        }
        Hasher hasher = Hashing.md5().newHasher();
        matches.forEach(m -> hasher
                .putInt(m.getMid().getId())
                .putInt(m.getUid().getId()));
        return hasher.hash().toString();
    }

    @Inject
    private TournamentService tournamentService;

    public void rescoreMatch(TournamentMemState tournament, RescoreMatch rescore, DbUpdater batch) {
        final MatchInfo mInfo = tournament.getMatchById(rescore.getMid());
        final Map<Uid, List<Integer>> newSets = rescore.getSets();
        validateRescoreMatch(tournament, mInfo, newSets);
        final List<MatchUid> affectedMatches = findEffectedMatches(tournament, mInfo, newSets);
        validateEffectHash(tournament, rescore, affectedMatches);

        final Optional<Uid> newWinner = findNewWinnerUid(tournament, newSets, mInfo);
        log.info("New winner {} in mid {}", newWinner, mInfo.getMid());
        reopenTournamentIfClosed(tournament, batch, affectedMatches, newWinner);
        truncateSets(batch, mInfo, 0);
        mInfo.loadParticipants(newSets);
        matchDao.insertScores(mInfo, batch);

        resetMatches(tournament, batch, affectedMatches);

        if (newWinner.isPresent()) {
            matchRescoreGivesWinner(tournament, batch, mInfo, newWinner, affectedMatches);
        } else {
            matchRescoreNoWinner(tournament, batch, mInfo);
        }
        scheduleService.afterMatchComplete(tournament, batch, clocker.get());
    }

    private void matchRescoreNoWinner(TournamentMemState tournament, DbUpdater batch, MatchInfo mInfo) {
        if (mInfo.getState() == Game) {
            log.info("Mid {} stays open", mInfo.getMid());
        } else { // request table scheduling
            log.info("Rescored mid {} returns to game", mInfo.getMid());
            mInfo.participants()
                    .map(tournament::getBidOrQuit)
                    .forEach(bid -> resetBidStateTo(batch, bid, Wait));
            resetBidStatesForRestGroupParticipants(tournament, mInfo, batch);
            matchService.changeStatus(batch, mInfo, Place);
        }
    }

    private void resetBidStatesForRestGroupParticipants(TournamentMemState tournament,
            MatchInfo mInfo, DbUpdater batch) {
        mInfo.getGid().ifPresent(gid -> {
            log.info("Reset rest lost bids to wait in gid {} of tid {}", gid, tournament.getTid());
            tournament.getParticipants().values().stream()
                    .filter(p -> p.getGid().equals(mInfo.getGid()))
                    .filter(p -> p.getBidState() == Lost)
                    .forEach(p -> bidService.setBidState(p, Wait, singleton(Lost), batch));
        });
    }

    private void matchRescoreGivesWinner(TournamentMemState tournament, DbUpdater batch,
            MatchInfo mInfo, Optional<Uid> newWinner, List<MatchUid> affectedMatches) {
        if (mInfo.getWinnerId().isPresent()) {
            final Map<Uid, BidState> playing = makeParticipantPlaying(tournament, batch, mInfo);
            if (!affectedMatches.isEmpty()) {
                mInfo.getGid().ifPresent(gid -> log.info("Reset bid states in group {}", gid));
                resetBidStatesForRestGroupParticipants(tournament, mInfo, batch);
            }
            matchService.matchWinnerDetermined(
                    tournament, mInfo, newWinner.get(), batch, OVER_EXPECTED);
            if (affectedMatches.isEmpty()) {
                mInfo.getGid().ifPresent(gid -> log.info("Recover bid states {}", gid));
                recoverPlayingStates(tournament, batch, playing);
            }
        } else { // was playing
            log.info("Rescored mid {} is complete", mInfo.getMid());
            matchService.matchWinnerDetermined(
                    tournament, mInfo, newWinner.get(), batch, GAME_EXPECTED);
        }
    }

    private Map<Uid, BidState> makeParticipantPlaying(TournamentMemState tournament,
            DbUpdater batch, MatchInfo mInfo) {
        Map<Uid, BidState> states = new HashMap<>();
        mInfo.participants()
                .map(tournament::getBidOrQuit)
                .peek(bid -> states.put(bid.getUid(), bid.getState()))
                .forEach(bid -> {
                    if (bid.getState() != Play) {
                        resetBidStateTo(batch, bid, Play);
                    }
                });
        return states;
    }

    private void recoverPlayingStates(TournamentMemState tournament, DbUpdater batch,
            Map<Uid, BidState> playing) {
        playing.entrySet()
                .forEach(e -> {
                    final ParticipantMemState bid = tournament.getBidOrQuit(e.getKey());
                    bidService.setBidState(bid, e.getValue(), singleton(bid.getState()), batch);
                });
    }

    private void reopenTournamentIfClosed(TournamentMemState tournament, DbUpdater batch,
            List<MatchUid> affectedMatches, Optional<Uid> newWinner) {
        if (tournament.getState() == Close) {
            if (!affectedMatches.isEmpty() || !newWinner.isPresent()) {
                tournamentService.setTournamentState(tournament, Open, batch);
            }
        }
    }

    private void validateEffectHash(TournamentMemState tournament,
            RescoreMatch rescore, List<MatchUid> effectedMatches) {
        final String presenteHash = rescore.getEffectHash();
        if (DONT_CHECK_HASH.equals(presenteHash)) {
            log.info("Skip hash check {} on rescoring mid {}", presenteHash, rescore.getMid());
            return;
        }
        final String expectedEffectHash = calculateEffectHash(effectedMatches);
        if (!expectedEffectHash.equals(presenteHash)) {
            throw badRequest(new EffectHashMismatchError(expectedEffectHash,
                    effectedMatches.stream()
                            .map(MatchUid::getMid)
                            .distinct()
                            .map(tournament::getMatchById)
                            .sorted(Comparator.comparing(MatchInfo::getLevel))
                            .map(m -> toEffectedMatch(tournament, m))
                            .collect(toList())));
        }
        log.info("Hash check passed for {} on rescoring mid {}",
                presenteHash, rescore.getMid());
    }

    private static final Set<TournamentState> openOrClose = ImmutableSet.of(Open, Close);

    private void validateRescoreMatch(TournamentMemState tournament, MatchInfo mInfo,
            Map<Uid, List<Integer>> newSets) {
        if (!openOrClose.contains(tournament.getState())) {
            throw badRequest("tournament is not open nor closed");
        }
        if (!rescorableMatchState.contains(mInfo.getState())) {
            throw badRequest("match is not in a rescorable state");
        }
        if (mInfo.getPlayedSets() == 0) {
            throw badRequest("match should have a scored set");
        }
        if (!mInfo.getParticipantIdScore().keySet().equals(newSets.keySet())) {
            throw badRequest("match has different participants");
        }
        final MatchValidationRule matchRules = tournament.getRule().getMatch();
        final Iterator<Uid> uidIterator = newSets.keySet().iterator();
        final Uid uid1 = uidIterator.next();
        final Uid uid2 = uidIterator.next();
        final List<Integer> score1 = newSets.get(uid1);
        final List<Integer> score2 = newSets.get(uid2);
        if (score1.size() != score2.size()) {
            throw badRequest("Set number mismatch");
        }
        if (score1.size() == 0) {
            throw badRequest("new match score has no any set");
        }
        for (int iset = 0; iset < score2.size(); ++iset) {
            matchRules.validateSet(
                    iset,
                    asList(IdentifiedScore.builder()
                                    .score(score1.get(iset))
                                    .uid(uid1)
                                    .build(),
                            IdentifiedScore.builder()
                                    .score(score2.get(iset))
                                    .uid(uid2)
                                    .build()));
        }
        matchRules.checkWonSets(matchRules.calcWonSets(newSets));
    }

    public void resetMatchScore(TournamentMemState tournament, ResetSetScore reset, DbUpdater batch) {
        final MatchInfo mInfo = tournament.getMatchById(reset.getMid());
        final int numberOfSets = mInfo.getPlayedSets();
        if (numberOfSets < reset.getSetNumber()) {
            throw badRequest("Match has just " + numberOfSets + " sets");
        }
        if (numberOfSets == reset.getSetNumber()) {
            return;
        }
        if (mInfo.getState() == Game) {
            truncateSets(batch, mInfo, reset.getSetNumber());
            return;
        }

        final Map<Uid, List<Integer>> newSets = mInfo.sliceFirstSets(reset.getSetNumber());
        final List<MatchUid> affectedMatches = findEffectedMatches(tournament, mInfo, newSets);
        truncateSets(batch, mInfo, reset.getSetNumber());
        resetMatches(tournament, batch, affectedMatches);
        matchService.changeStatus(batch, mInfo, Game);

        resetMatches(tournament, batch, affectedMatches);

        reopenTournamentIfClosed(tournament, batch, affectedMatches, Optional.empty());
        scheduleService.afterMatchComplete(tournament, batch, clocker.get());
    }

    private void resetMatches(TournamentMemState tournament, DbUpdater batch,
            List<MatchUid> affectedMatches) {
        affectedMatches.forEach(
                aMatch -> removeParticipant(
                        tournament, batch,
                        tournament.getMatchById(aMatch.getMid()),
                        aMatch.getUid()));
    }

    private void removeParticipant(TournamentMemState tournament,
            DbUpdater batch, MatchInfo mInfo, Uid uid) {
        final int played = mInfo.getPlayedSets();
        if (!mInfo.removeParticipant(uid)) {
            log.warn("No uid {} is not in mid {}", uid, mInfo.getMid());
            return;
        }
        matchDao.removeScores(batch, mInfo.getMid(), uid, played);
        mInfo.leftUid().ifPresent(ouid -> {
            matchDao.removeScores(batch, mInfo.getMid(), ouid, played);
            mInfo.getParticipantScore(ouid).clear();
        });
        final int numberOfParticipants = mInfo.numberOfParticipants();
        if (numberOfParticipants == 1) {
            log.warn("Remove first uid {} from mid {}", uid, mInfo.getMid());
            final Uid opUid = mInfo.leftUid().get();
            final ParticipantMemState opponent = tournament.getBidOrQuit(opUid);
            final BidState opoState = opponent.getState();
            switch (opoState) {
                case Expl:
                case Quit:
                    matchService.changeStatus(batch, mInfo, Auto);
                    break;
                default:
                    matchService.changeStatus(batch, mInfo, Draft);
                    break;
            }
            resetBidStateTo(batch, opponent, Wait);
            final ParticipantMemState bid = tournament.getBidOrQuit(uid);
            resetBidStateTo(batch, bid, Wait);
            matchDao.removeSecondParticipant(batch, mInfo.getMid(), opUid);
        } else if (numberOfParticipants == 0) {
            log.warn("Remove last uid {} from mid {}", uid, mInfo.getMid());
            matchService.changeStatus(batch, mInfo, Draft);
            matchDao.removeParticipants(batch, mInfo.getMid());
        } else {
            throw internalError("unexpected number or participants left "
                    + numberOfParticipants);
        }
    }

    private void resetBidStateTo(DbUpdater batch, ParticipantMemState opponent, BidState targetState) {
        if (TERMINAL_RECOVERABLE_STATES.contains(opponent.getState())) {
            bidService.setBidState(opponent, targetState,
                    TERMINAL_RECOVERABLE_STATES, batch);
        }
    }

    private boolean allMatchesInGroupComplete(List<MatchInfo> groupMatches) {
        return groupMatches.stream().allMatch(minfo -> minfo.getState() == Over);
    }

    private void truncateSets(DbUpdater batch, MatchInfo minfo, int setNumber) {
        matchDao.deleteSets(batch, minfo, setNumber);
        cutTrailingSets(minfo, setNumber);
    }

    private void cutTrailingSets(MatchInfo minfo, int setNumber) {
        minfo.getParticipantIdScore().values()
                .forEach(scores -> scores.subList(setNumber, scores.size()).clear());
    }
}
