package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidService.WIN_STATES;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Rest;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.tournament.MatchScoreResult.MatchContinues;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.group.PlayOffMatcherFromGroup;
import org.dan.ping.pong.app.place.PlaceMemState;
import org.dan.ping.pong.app.place.PlaceService;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.table.TableService;
import org.dan.ping.pong.app.tournament.ConfirmSetScore;
import org.dan.ping.pong.app.tournament.DbUpdater;
import org.dan.ping.pong.app.tournament.DbUpdaterFactory;
import org.dan.ping.pong.app.tournament.MatchScoreResult;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentCache;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class MatchService {
    @Inject
    private MatchDao matchDao;

    @Inject
    private TournamentDao tournamentDao;

    @Inject
    private Clocker clocker;

    @Inject
    private TableDao tableDao;

    @Inject
    private TableService tableService;

    @Inject
    private BidDao bidDao;

    @Inject
    private TournamentCache tournamentCache;

    @Inject
    private SequentialExecutor sequentialExecutor;

    @Value("${match.score.timeout}")
    private Duration matchScoreTimeout;

    @Inject
    private DbUpdaterFactory dbUpdaterFactory;

    @Inject
    private PlaceService placeService;

    public MatchScoreResult scoreSet(int uid, FinalMatchScore score, Instant now) {
        final OpenTournamentMemState tournament = tournamentCache
                .load(score.getTid());
        tournament.getRule().getMatch().validateSet(score.getScores());
        final MatchInfo matchInfo = tournament.getMatchById(score.getMid());
        try {
            checkPermissions(tournament, uid, matchInfo, score);
        } catch (ConfirmSetScore e) {
            log.info("User {} confirmed set score in mid {}", uid, score.getMid());
            return matchInfo.getState() == Over
                    ? tournament.matchScoreResult()
                    : MatchContinues;
        }
        final DbUpdater batch = dbUpdaterFactory.create()
                .onFailure(() -> tournamentCache.invalidate(score.getTid()));
        try {
            final Optional<Integer> winUidO = matchDao.scoreSet(tournament, matchInfo, batch, score);
            winUidO.ifPresent(winUid -> matchWinnerDetermined(tournament, matchInfo, winUid, batch));
            return sequentialExecutor.executeSync(tournament.getPid(),
                    matchScoreTimeout,
                    () -> freeTableAndSchedule(batch, tournament, now, score, winUidO));
        } finally {
            batch.rollback();
        }
    }

    MatchScoreResult freeTableAndSchedule(DbUpdater batch, OpenTournamentMemState tournament,
            Instant now, FinalMatchScore score, Optional<Integer> winUidO) {
        final PlaceMemState place = placeService.load(tournament.getPid());
        batch.onFailure(() -> placeService.invalidate(tournament.getPid()));
        tableService.freeTable(place, score.getMid(), batch);
        tableService.scheduleFreeTables(tournament, place, now, batch);
        batch.flush();
        return winUidO.map(u -> tournament.matchScoreResult())
                .orElse(MatchScoreResult.MatchContinues);
    }

    private void matchWinnerDetermined(OpenTournamentMemState tournament, MatchInfo matchInfo,
            int winUid, DbUpdater batch) {
        completeMatch(matchInfo, winUid, batch);
        if (matchInfo.getGid().isPresent()) {
            tryToCompleteGroup(tournament, matchInfo, batch);
        } else {
            completePlayOffMatch(tournament, matchInfo, winUid, batch);
        }
    }

    private void completeMatch(MatchInfo matchInfo, int winUid, DbUpdater batch) {
        matchInfo.setState(Over);
        matchInfo.setWinnerId(Optional.of(winUid));
        matchDao.completeMatch(matchInfo.getMid(), winUid, clocker.get(), batch);
    }

    private void completeNoLastPlayOffMatch(OpenTournamentMemState tournament, MatchInfo matchInfo,
            int winUid, DbUpdater batch) {
        ParticipantMemState winBid = tournament.getParticipants().get(winUid);
        bidService.setBidState(winBid, Wait, singletonList(Play), batch);
        assignBidToMatch(tournament, matchInfo.getWinnerMid().get(), winUid, batch);
        final int lostUid = matchInfo.getLoserUid(winUid).get();
        if (matchInfo.getLoserMid().isPresent()) {
            assignBidToMatch(tournament, matchInfo.getLoserMid().get(), lostUid, batch);
        } else {
            bidService.setBidState(tournament.getParticipants().get(lostUid),
                    Lost, asList(Play, Rest), batch);
        }
    }

    private void completeLastPlayOffMatch(OpenTournamentMemState tournament, MatchInfo matchInfo,
            int winUid, DbUpdater batch) {
        switch (matchInfo.getType()) {
            case Gold:
                setMedalMatchBidStatuses(matchInfo, winUid, batch, Win1, Win2);
                break;
            case Brnz:
                setMedalMatchBidStatuses(matchInfo, winUid, batch, Win3, Lost);
                break;
            default:
                throw internalError("Match type "
                        + matchInfo.getType()
                        + " is not terminal");
        }
        tournamentService.endOfTournamentCategory(tournament, matchInfo.getCid(), batch);
    }

    private void setMedalMatchBidStatuses(MatchInfo matchInfo, int winUid, DbUpdater batch,
            BidState win, BidState lost) {
        matchInfo.getParticipantIdScore().keySet().forEach(bid ->
                bidDao.setBidState(matchInfo.getTid(), bid,
                        winUid == bid ? win : lost,
                        asList(Play, Rest),
                        clocker.get(), batch));
    }

    private void completePlayOffMatch(OpenTournamentMemState tournament, MatchInfo matchInfo,
            int winUid, DbUpdater batch) {
        if (matchInfo.getWinnerMid().isPresent()) {
            completeNoLastPlayOffMatch(tournament, matchInfo, winUid, batch);
        } else {
            completeLastPlayOffMatch(tournament, matchInfo, winUid, batch);
        }
    }

    @Inject
    private TournamentService tournamentService;

    @Inject
    private GroupDao groupDao;

    private List<MatchInfo> findMatchesInGroup(OpenTournamentMemState tournament, int gid) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getGid().equals(Optional.of(gid)))
                .collect(toList());
    }

    public void tryToCompleteGroup(OpenTournamentMemState tournament, MatchInfo matchInfo, DbUpdater batch) {
        final int gid = matchInfo.getGid().get();
        final int tid = matchInfo.getTid();
        final Set<Integer> uids = matchInfo.getParticipantIdScore().keySet();
        final List<MatchInfo> matches = findMatchesInGroup(tournament, gid);
        final long completedMatches = matches.stream()
                .map(MatchInfo::getState)
                .filter(Over::equals)
                .count();
        uids.forEach(bid -> bidDao.setBidState(tid, bid, Wait, Play, clocker.get(), batch));
        if (completedMatches < matches.size()) {
            log.debug("Matches {} left to play in the group {}",
                    matches.size() - completedMatches, gid);
            return;
        }
        completeParticipationLeftBids(tid, completeGroup(gid, tournament, matches, batch), batch);
    }

    private void completeParticipationLeftBids(int tid, List<Integer> leftUids, DbUpdater batch) {
        final Instant now = clocker.get();
        leftUids.forEach(uid -> bidDao.setBidState(tid, uid, Lost, asList(Wait, Rest), now, batch));
    }

    @Inject
    private GroupService groupService;

    List<MatchInfo> findPlayOffMatches(OpenTournamentMemState tournament, int cid) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getCid() == cid)
                .filter(minfo -> !minfo.getGid().isPresent())
                .sorted(Comparator.comparingInt(MatchInfo::getMid))
                .collect(toList());
    }

    @Inject
    private BidService bidService;

    private void completeMiniTournamentGroup(
            OpenTournamentMemState tournament, GroupInfo iGru,
            List<Integer> quitUids, DbUpdater batch) {
        if (iGru.getOrdNumber() == 0) {
            for (int i = 0; i < quitUids.size(); ++i) {
                bidService.setBidState(tournament.getParticipants().get(quitUids.get(i)),
                        WIN_STATES.get(i), asList(Wait, Quit, Lost, Rest), batch);
            }
            tournamentService.endOfTournamentCategory(tournament, iGru.getCid(), batch);
        } else {
            throw internalError("Tournament " +
                    tournament.getTid() + " in category " + iGru.getCid()
                    + " doesn't have playoff matches");
        }
    }

    private List<Integer> completeGroup(Integer gid, OpenTournamentMemState tournament,
            List<MatchInfo> matches, DbUpdater batch) {
        log.info("Pick bids for playoff from gid {} in tid {}", gid, tournament.getTid());
        final int quits = tournament.getRule().getGroup().getQuits();
        final List<Integer> orderUids = groupService.orderUidsInGroup(tournament, matches);
        final List<Integer> quitUids = orderUids.subList(0, quits);
        final GroupInfo iGru = tournament.getGroups().get(gid);
        final List<MatchInfo> playOffMatches = findPlayOffMatches(tournament, iGru.getCid());
        if (playOffMatches.isEmpty()) {
            completeMiniTournamentGroup(tournament, iGru, quitUids, batch);
        } else {
            final List<GroupInfo> groups = tournament.getGroupsByCategory(iGru.getCid());
            final int expectedPlayOffMatches = quits * groups.size() / 2;
            if (playOffMatches.size() != expectedPlayOffMatches) {
                throw internalError("Mismatch in number of play off matches "
                        + expectedPlayOffMatches + " !=" + playOffMatches.size()
                        + " group " + iGru.getGid());
            }
            final List<Integer> matchIndexes = PlayOffMatcherFromGroup.find(
                    quits, iGru.getOrdNumber(), groups.size());
            for (int iQuitter = 0; iQuitter < quits; ++iQuitter) {
                int matchIdx = matchIndexes.get(iQuitter);
                assignBidToMatch(tournament, playOffMatches.get(matchIdx).getMid(),
                        quitUids.get(iQuitter), batch);
            }
            return quitUids;
        }
        return orderUids.subList(quits, orderUids.size());
    }

    private void assignBidToMatch(OpenTournamentMemState tournament, int mid, int uid, DbUpdater batch) {
        final MatchInfo matchInfo = tournament.getMatchById(mid);
        if (matchInfo.getParticipantIdScore().size() == 2) {
            throw internalError("Match " + matchInfo.getMid() + " gets 3rd participant");
        }
        matchDao.setParticipant(matchInfo.getParticipantIdScore().size(),
                matchInfo.getMid(), uid, batch);
        matchInfo.getParticipantIdScore().put(uid, new ArrayList<>());
        if (matchInfo.getParticipantIdScore().size() == 2) {
            switch (matchInfo.getState()) {
                case Draft:
                    changeStatus(batch, matchInfo);
                    break;
                case Auto:
                    autoWinComplete(tournament, matchInfo, uid, batch);
                    break;
                default:
                    throw internalError("Unexpected state "
                            + matchInfo.getState() + " in " + mid);
            }
        }
    }

    private void autoWinComplete(OpenTournamentMemState tournament, MatchInfo matchInfo, int uid, DbUpdater batch) {
        completeMatch(matchInfo, uid, batch);
        matchInfo.getWinnerMid()
                .ifPresent(wmid -> assignBidToMatch(tournament, wmid, uid, batch));
        switch (matchInfo.getType()) {
            case Brnz:
                bidService.setBidState(tournament.getBid(uid), Win3, asList(Play, Wait, Rest), batch);
                break;
            case Gold:
                bidService.setBidState(tournament.getBid(uid), Win1, asList(Play, Wait, Rest), batch);
                break;
            case Grup:
            case POff:
                // skip
                break;
            default:
                throw internalError("Unexpected state " + matchInfo.getType());
        }
    }

    private void changeStatus(DbUpdater batch, MatchInfo matchInfo) {
        matchDao.changeStatus(matchInfo.getMid(), Place, batch);
        matchInfo.setState(Place);
    }

    private void checkPermissions(OpenTournamentMemState tournament, int senderUid,
            MatchInfo matchInfo, FinalMatchScore score) {
        final Set<Integer> scoreUids = score.getScores().stream()
                .map(IdentifiedScore::getUid).collect(toSet());
        final Set<Integer> participantIds = matchInfo.getParticipantIdScore().keySet();
        if (!participantIds.equals(scoreUids)) {
            throw badRequest("Some of participants don't play match "
                    + matchInfo.getMid());
        }
        if (!participantIds.contains(senderUid)
                && !tournament.isAdminOf(senderUid)) {
            throw forbidden("User " + senderUid
                    + " neither admin of the tournament"
                    + " nor a participant of the match");
        }
        final Map<Integer, Integer> newSetScore = score.getScores().stream()
                .collect(toMap(IdentifiedScore::getUid, IdentifiedScore::getScore));
        final int playedSets = matchInfo.getPlayedSets();
        if (playedSets < score.getSetOrdNumber()) {
            throw badRequest("Set " + playedSets + " needs to be scored first");
        }
        if (playedSets > score.getSetOrdNumber()) {
            final Map<Integer, Integer> setScore = matchInfo.getSetScore(score.getSetOrdNumber());
            if (newSetScore.equals(setScore)) {
                throw new ConfirmSetScore();
            }
            throw badRequest(new MatchScoredError());
        }
    }
}
