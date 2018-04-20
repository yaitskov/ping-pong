package org.dan.ping.pong.app.match;

import static java.util.Collections.singleton;
import static org.dan.ping.pong.app.bid.BidService.TERMINAL_RECOVERABLE_STATES;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.match.MatchState.Auto;
import static org.dan.ping.pong.app.match.MatchState.Draft;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.match.dispute.MatchSets.ofSets;
import static org.dan.ping.pong.app.sched.ScheduleCtx.SCHEDULE_SELECTOR;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.dispute.MatchSets;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.sched.ScheduleService;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.app.tournament.TournamentTerminator;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class MatchEditorService {
    private static final Set<MatchState> GAME_PLACE_EXPECTED = ImmutableSet.of(Place, Game);
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

    @Inject
    private TournamentService tournamentService;

    public void rescoreMatch(TournamentMemState tournament, RescoreMatch rescore, DbUpdater batch) {
        final MatchInfo rescoringMatch = tournament.getMatchById(rescore.getMid());
        final MatchSets newSets = ofSets(rescore.getSets());
        validateRescoreMatch(tournament, rescoringMatch, newSets);
        final AffectedMatches affectedMatches = affectedMatchesService
                .findEffectedMatches(tournament, rescoringMatch, newSets);
        affectedMatchesService.validateEffectHash(tournament, rescore, affectedMatches);

        final Optional<Uid> newWinner = sports.findNewWinnerUid(tournament, newSets, rescoringMatch);
        log.info("New winner {} in mid {}", newWinner, rescoringMatch.getMid());
        reopenTournamentIfOpenMatch(tournament, batch, affectedMatches, newWinner);
        overrideMatchSets(batch, rescoringMatch, newSets);

        if (newWinner.isPresent()) {
            matchRescoreGivesWinner(tournament, batch, rescoringMatch, newWinner, affectedMatches);
        } else {
            matchRescoreNoWinner(tournament, batch, rescoringMatch, affectedMatches);
        }
        scheduleService.afterMatchComplete(tournament, batch, clocker.get());
    }

    private void overrideMatchSets(DbUpdater batch, MatchInfo rescoringMatch, MatchSets newSets) {
        truncateSets(batch, rescoringMatch, 0);
        rescoringMatch.loadParticipants(newSets);
        matchDao.insertScores(rescoringMatch, batch);
    }

    private void matchRescoreNoWinner(TournamentMemState tournament, DbUpdater batch,
            MatchInfo mInfo, AffectedMatches affectedMatches) {
        if (mInfo.getState() == Game) {
            log.info("Mid {} stays open", mInfo.getMid());
        } else { // request table scheduling
            removeWinnerUidIf(batch, mInfo);
            resetMatches(tournament, batch, affectedMatches);
            log.info("Rescored mid {} returns to game", mInfo.getMid());
            mInfo.participants()
                    .map(tournament::getBidOrQuit)
                    .forEach(bid -> resetBidStateTo(batch, bid,
                            matchService.completeGroupMatchBidState(tournament, bid)));
            resetBidStatesForRestGroupParticipants(tournament, mInfo, batch);
            matchService.changeStatus(batch, mInfo, Place);
        }
    }

    private void removeWinnerUidIf(DbUpdater batch, MatchInfo mInfo) {
        mInfo.getWinnerId().ifPresent(wId -> {
            mInfo.setWinnerId(Optional.empty());
            matchDao.setWinnerId(mInfo, batch);
        });
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
            MatchInfo mInfo, Optional<Uid> newWinner, AffectedMatches affectedMatches) {
        if (matchHadWinner(mInfo)) {
            removeWinnerUidIf(batch, mInfo);
            resetMatches(tournament, batch, affectedMatches);
            makeParticipantPlaying(tournament, batch, mInfo);
            resetBidStatesForRestGroupParticipants(tournament, mInfo, batch);
            matchService.matchWinnerDetermined(
                    tournament, mInfo, newWinner.get(), batch, OVER_EXPECTED);
        } else { // was playing
            log.info("Rescored mid {} is complete", mInfo.getMid());
            matchService.matchWinnerDetermined(
                    tournament, mInfo, newWinner.get(), batch, GAME_PLACE_EXPECTED);
        }
    }

    private boolean matchHadWinner(MatchInfo mInfo) {
        return mInfo.getWinnerId().isPresent();
    }

    private void makeParticipantPlaying(TournamentMemState tournament,
            DbUpdater batch, MatchInfo mInfo) {
        mInfo.participants()
                .map(tournament::getBidOrQuit)
                .forEach(bid -> {
                    if (bid.getState() != Play) {
                        resetBidStateTo(batch, bid, Play);
                    }
                });
    }

    @Inject
    private TournamentTerminator tournamentTerminator;

    private void reopenTournamentIfOpenMatch(TournamentMemState tournament, DbUpdater batch,
            AffectedMatches affectedMatches, Optional<Uid> newWinner) {
        if (tournament.getState() == Close) {
            if (!affectedMatches.getToBeCreated().isEmpty()
                    || !affectedMatches.getToBeReset().isEmpty()
                    || !newWinner.isPresent()) {
                tournamentTerminator.setTournamentState(tournament, Open, batch);
            }
        }
    }

    @Inject
    private AffectedMatchesService affectedMatchesService;

    private static final Set<TournamentState> openOrClose = ImmutableSet.of(Open, Close);

    @Inject
    private Sports sports;

    private void validateRescoreMatch(TournamentMemState tournament, MatchInfo mInfo,
            MatchSets newSets) {
        if (!openOrClose.contains(tournament.getState())) {
            throw badRequest("tournament is not open nor closed");
        }
        if (mInfo.getPlayedSets() == 0) {
            throw badRequest("match should have a scored set");
        }
        newSets.validateParticipants(mInfo.getParticipantIdScore().keySet());
        if (newSets.validateEqualNumberSets() == 0) {
            throw badRequest("new match score has no any set");
        }

        final MatchInfo mInfoExpectedAfter = sports.alternativeSetsWithoutWinner(mInfo, newSets);
        sports.validateMatch(tournament, mInfoExpectedAfter);
        sports.checkWonSets(tournament, sports.calcWonSets(tournament, mInfoExpectedAfter));
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

        final MatchSets newSets = mInfo.sliceFirstSets(reset.getSetNumber());
        final AffectedMatches affectedMatches = affectedMatchesService
                .findEffectedMatches(tournament, mInfo, newSets);
        truncateSets(batch, mInfo, reset.getSetNumber());
        resetMatches(tournament, batch, affectedMatches);
        matchService.changeStatus(batch, mInfo, Game);

        resetMatches(tournament, batch, affectedMatches);

        reopenTournamentIfOpenMatch(tournament, batch, affectedMatches, Optional.empty());
        scheduleService.afterMatchComplete(tournament, batch, clocker.get());
    }

    private void resetMatches(TournamentMemState tournament, DbUpdater batch,
            AffectedMatches affectedMatches) {
        affectedMatches.getToBeReset().forEach(
                aMatch -> removeParticipant(
                        tournament, batch,
                        tournament.getMatchById(aMatch.getMid()),
                        aMatch.getUid()));
        tournamentService.deleteByMids(tournament, batch, affectedMatches.getToBeRemoved());

        affectedMatches.getToBeCreated().forEach(
                mp -> groupService.createDisambiguateMatches(tournament,
                        tournament.getParticipant(mp.getUidLess()).gid(), mp));
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
            removeWinnerUidIf(batch, mInfo);
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

    private void resetBidStateTo(DbUpdater batch, ParticipantMemState opponent,
            BidState targetState) {
        if (TERMINAL_RECOVERABLE_STATES.contains(opponent.getState())) {
            bidService.setBidState(opponent, targetState,
                    TERMINAL_RECOVERABLE_STATES, batch);
        }
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
