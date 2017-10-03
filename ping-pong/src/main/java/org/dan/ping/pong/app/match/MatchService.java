package org.dan.ping.pong.app.match;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidService.WIN_STATES;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Rest;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.app.match.MatchState.Auto;
import static org.dan.ping.pong.app.match.MatchState.Draft;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_UID;
import static org.dan.ping.pong.app.tournament.SetScoreResultName.MatchContinues;
import static org.dan.ping.pong.app.tournament.TournamentState.Canceled;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.app.tournament.TournamentState.Replaced;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.group.PlayOffMatcherFromGroup;
import org.dan.ping.pong.app.place.PlaceMemState;
import org.dan.ping.pong.app.place.PlaceService;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.table.TableService;
import org.dan.ping.pong.app.tournament.ConfirmSetScore;
import org.dan.ping.pong.app.tournament.DbUpdater;
import org.dan.ping.pong.app.tournament.DbUpdaterFactory;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.SetScoreResultName;
import org.dan.ping.pong.app.tournament.TournamentCache;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.beans.factory.annotation.Value;

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
    private TournamentCache tournamentCache;

    @Inject
    private SequentialExecutor sequentialExecutor;

    @Value("${match.score.timeout}")
    private int matchScoreTimeout;

    @Inject
    private DbUpdaterFactory dbUpdaterFactory;

    @Inject
    private PlaceService placeService;

    public int roundPlayOffBase(int bids) {
        int places = 1;
        while (places < bids) {
            places *= 2;
        }
        return places;
    }

    private static final Set<TournamentState> TERMINAL_STATE = ImmutableSet.of(Close, Canceled, Replaced);

    public SetScoreResult scoreSet(OpenTournamentMemState tournament, int uid,
            FinalMatchScore score, Instant now, DbUpdater batch) {
        tournament.getRule().getMatch().validateSet(score.getScores());
        final MatchInfo matchInfo = tournament.getMatchById(score.getMid());
        try {
            checkPermissions(tournament, uid, matchInfo, score);
        } catch (ConfirmSetScore e) {
            log.info("User {} confirmed set score in mid {}", uid, score.getMid());
            return setScoreResult(tournament,
                    matchInfo.getState() == Over
                            ? tournament.matchScoreResult()
                            : MatchContinues,
                    matchInfo);
        }
        final Optional<Integer> winUidO = matchDao.scoreSet(tournament, matchInfo, batch, score);
        winUidO.ifPresent(winUid -> matchWinnerDetermined(tournament, matchInfo, winUid, batch));
        return sequentialExecutor.executeSync(placeService.load(tournament.getPid()),
                place -> {
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    if (TERMINAL_STATE.contains(tournament.getState())) {
                        tableService.bindPlace(place, batch, Optional.empty());
                    }
                    return setScoreResult(
                            tournament,
                            freeTableAndSchedule(place, batch, tournament, now, winUidO),
                            matchInfo);
                });
    }

    SetScoreResult setScoreResult(OpenTournamentMemState tournament, SetScoreResultName name, MatchInfo matchInfo) {
        switch (name) {
            case LastMatchComplete:
            case MatchComplete:
                return SetScoreResult.builder()
                        .matchScore(Optional.of(matchScore(tournament, matchInfo)))
                        .scoreOutcome(name)
                        .build();
            case MatchContinues:
                return SetScoreResult.builder()
                        .scoreOutcome(name)
                        .nextSetNumberToScore(Optional.of(matchInfo.getNumberOfSets()))
                        .build();
            default:
                throw internalError("Unknown state " + name);
        }
    }

    SetScoreResultName freeTableAndSchedule(PlaceMemState place, DbUpdater batch,
            OpenTournamentMemState tournament,
            Instant now, Optional<Integer> winUidO) {
        batch.onFailure(() -> placeService.invalidate(tournament.getPid()));
        tableService.scheduleFreeTables(tournament, place, now, batch);
        return winUidO.map(u -> tournament.matchScoreResult())
                .orElse(SetScoreResultName.MatchContinues);
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
        matchDao.completeMatch(matchInfo.getMid(), winUid, clocker.get(), batch, Game, Place, Auto);
    }

    private void completeNoLastPlayOffMatch(OpenTournamentMemState tournament, MatchInfo matchInfo,
            int winUid, DbUpdater batch) {
        ParticipantMemState winBid = tournament.getParticipants().get(winUid);
        bidService.setBidState(winBid, Wait, singletonList(Play), batch);
        assignBidToMatch(tournament, matchInfo.getWinnerMid().get(), winUid, batch);
        final int lostUid = matchInfo.getOpponentUid(winUid).get();
        final ParticipantMemState lostBid = tournament.getParticipants().get(lostUid);
        if (lostBid == null) {
            checkArgument(lostUid == FILLER_LOSER_UID);
        }
        if (matchInfo.getLoserMid().isPresent()) {
            if (lostBid != null) {
                bidService.setBidState(lostBid, Wait, asList(Play, Rest), batch);
            }
            assignBidToMatch(tournament, matchInfo.getLoserMid().get(), lostUid, batch);
        } else {
            if (lostBid == null) {
                return;
            }
            bidService.setBidState(lostBid,
                    matchInfo.getType() == MatchType.Brnz
                            ? Win3
                            : Lost, asList(Play, Wait, Rest), batch);
        }
    }

    private void completeLastPlayOffMatch(OpenTournamentMemState tournament, MatchInfo matchInfo,
            int winUid, DbUpdater batch) {
        switch (matchInfo.getType()) {
            case Gold:
                setMedalMatchBidStatuses(tournament, matchInfo, winUid, batch, Win1, Win2);
                break;
            case Brnz:
                setMedalMatchBidStatuses(tournament, matchInfo, winUid, batch, Win3, Lost);
                break;
            default:
                throw internalError("Match type "
                        + matchInfo.getType()
                        + " is not terminal");
        }
        tournamentService.endOfTournamentCategory(tournament, matchInfo.getCid(), batch);
    }

    private void setMedalMatchBidStatuses(OpenTournamentMemState tournament, MatchInfo matchInfo, int winUid, DbUpdater batch,
            BidState win, BidState lost) {
        matchInfo.getParticipantIdScore().keySet().forEach(uid ->
                bidService.setBidState(tournament.getParticipant(uid),
                        winUid == uid ? win : lost, asList(Play, Rest), batch));
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
        final Set<Integer> uids = matchInfo.getParticipantIdScore().keySet();
        final List<MatchInfo> matches = findMatchesInGroup(tournament, gid);
        final long completedMatches = matches.stream()
                .map(MatchInfo::getState)
                .filter(Over::equals)
                .count();
        uids.forEach(uid -> bidService.setBidState(tournament.getParticipant(uid), Wait,
                singletonList(Play), batch));
        if (completedMatches < matches.size()) {
            log.debug("Matches {} left to play in the group {}",
                    matches.size() - completedMatches, gid);
            return;
        }
        completeParticipationLeftBids(tournament, completeGroup(gid, tournament, matches, batch), batch);
    }

    private void completeParticipationLeftBids(OpenTournamentMemState tournament,
            List<Integer> leftUids, DbUpdater batch) {
        leftUids.forEach(uid -> {
            final ParticipantMemState participant = tournament.getParticipant(uid);
            if (participant.getState() == Quit || participant.getState() == Expl) {
                return;
            }
            bidService.setBidState(participant, Lost, asList(Wait, Rest), batch);
        });
    }

    @Inject
    private GroupService groupService;

    @Inject
    private BidService bidService;

    private void completeMiniTournamentGroup(
            OpenTournamentMemState tournament, GroupInfo iGru,
            List<Integer> quitUids, DbUpdater batch) {
        if (iGru.getOrdNumber() == 0) {
            for (int i = 0; i < quitUids.size(); ++i) {
                bidService.setBidState(tournament.getParticipant(quitUids.get(i)),
                        i < WIN_STATES.size()
                                ? WIN_STATES.get(i)
                                : Lost,
                        asList(Play, Wait, Quit, Lost, Rest), batch);
            }
            tournamentService.endOfTournamentCategory(tournament, iGru.getCid(), batch);
        } else {
            throw internalError("Tournament " +
                    tournament.getTid() + " in category " + iGru.getCid()
                    + " doesn't have playoff matches");
        }
    }

    @Inject
    private PlayOffService playOffService;

    private List<Integer> completeGroup(Integer gid, OpenTournamentMemState tournament,
            List<MatchInfo> matches, DbUpdater batch) {
        log.info("Pick bids for playoff from gid {} in tid {}", gid, tournament.getTid());
        final int quits = tournament.getRule().getGroup().get().getQuits();
        final List<Integer> orderUids = groupService.orderUidsInGroup(tournament, matches);
        final List<Integer> quitUids = orderUids.subList(0, quits);
        log.info("{} quit group {}", quitUids, gid);
        final GroupInfo iGru = tournament.getGroups().get(gid);
        final List<MatchInfo> playOffMatches = playOffService.findBaseMatches(
                playOffService.findPlayOffMatches(tournament, iGru.getCid())).stream()
                .sorted(Comparator.comparingInt(MatchInfo::getMid))
                .collect(toList());
        if (playOffMatches.isEmpty()) {
            completeMiniTournamentGroup(tournament, iGru, quitUids, batch);
        } else {
            final List<GroupInfo> groups = tournament.getGroupsByCategory(iGru.getCid());
            final int expectedPlayOffMatches = roundPlayOffBase(quits * groups.size()) / 2;
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
        }
        return orderUids.subList(quits, orderUids.size());
    }

    public void assignBidToMatch(OpenTournamentMemState tournament, int mid, int uid, DbUpdater batch) {
        log.info("Assign uid {} to mid {} in tid {}", uid, mid, tournament.getTid());
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
                    changeStatus(batch, matchInfo, Place);
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
        log.info("Auto complete mid {} due {} quit and {} detected",
                matchInfo.getMid(), matchInfo.getOpponentUid(uid), uid);
        completeMatch(matchInfo, uid, batch);
        matchInfo.getWinnerMid()
                .ifPresent(wmid -> assignBidToMatch(tournament, wmid, uid, batch));
        if (uid == FILLER_LOSER_UID) {
            return;
        }
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

    private void changeStatus(DbUpdater batch, MatchInfo matchInfo, MatchState state) {
        if (matchInfo.getState() == state) {
            return;
        }
        matchInfo.setState(state);
        matchDao.changeStatus(matchInfo.getMid(), state, batch);
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
            throw badRequest(new MatchScoredError(matchScore(tournament, matchInfo)));
        }
    }

    public MatchScore matchScore(OpenTournamentMemState tournament, MatchInfo matchInfo) {
        return MatchScore.builder()
                .mid(matchInfo.getMid())
                .tid(matchInfo.getTid())
                .winUid(matchInfo.getWinnerId())
                .sets(matchInfo.getParticipantIdScore())
                .wonSets(tournament.getRule().getMatch()
                        .calcWonSets(matchInfo.getParticipantIdScore()))
                .build();
    }

    private static final Set<MatchState> incompleteStates = ImmutableSet.of(Draft, Place, Game);

    public List<MatchInfo> bidIncompleteGroupMatches(int uid, OpenTournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getParticipantIdScore().containsKey(uid))
                .filter(minfo -> minfo.getGid().isPresent())
                .filter(minfo -> incompleteStates.contains(minfo.getState()))
                .collect(toList());
    }

    public void leaveFromPlayOff(ParticipantMemState bid, OpenTournamentMemState tournament, DbUpdater batch) {
        playOffMatchForResign(bid.getUid().getId(), tournament)
                .ifPresent(match -> {
                    walkOver(tournament, bid.getUid().getId(), match, batch);
                    leaveFromPlayOff(bid, tournament, batch);
                });
    }

    public void walkOver(OpenTournamentMemState tournament, int walkoverUid, MatchInfo matchInfo, DbUpdater batch) {
        log.info("Uid {} walkovers mid {}", walkoverUid, matchInfo.getMid());
        Optional<Integer> winUid = matchInfo.getOpponentUid(walkoverUid);
        if (winUid.isPresent()) {
            matchWinnerDetermined(tournament, matchInfo, winUid.get(), batch);
        } else {
            changeStatus(batch, matchInfo, Auto);
            matchInfo.getLoserMid()
                    .ifPresent(lmid -> assignBidToMatch(tournament, lmid, walkoverUid, batch));
        }
    }

    public Optional<MatchInfo> playOffMatchForResign(int uid, OpenTournamentMemState tournament) {
        return tournament.getMatches().values()
                .stream()
                .filter(minfo -> minfo.hasParticipant(uid))
                .filter(minfo -> !minfo.getGid().isPresent())
                .filter(minfo -> ImmutableSet.of(Game, Place, Draft).contains(minfo.getState()))
                .findAny();
    }

    @Inject
    private PlaceService placeCache;

    public List<OpenMatchForWatch> findOpenMatchesForWatching(OpenTournamentMemState tournament) {
        return sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> tournament.getMatches().values().stream()
                        .filter(m -> m.getState() == Game)
                        .map(m -> OpenMatchForWatch.builder()
                                .mid(m.getMid())
                                .started(m.getStartedAt().get())
                                .score(tournament.getRule().getMatch()
                                        .calcWonSets(m.getParticipantIdScore())
                                        .values()
                                        .stream().collect(toList()))
                                .category(tournament.getCategory(m.getCid()))
                                .table(place.getTableByMid(m.getMid()).toLink())
                                .type(m.getType())
                                .participants(
                                        m.getUids().stream()
                                                .map(tournament::getParticipant)
                                                .map(ParticipantMemState::toLink)
                                                .collect(toList()))
                                .build())
                        .collect(toList()));
    }

    public void markAsSchedule(MatchInfo match, Instant now, DbUpdater batch) {
        match.setState(Game);
        match.setStartedAt(Optional.of(now));
        matchDao.markAsSchedule(match, batch);
    }

    public void resetMatchScore(OpenTournamentMemState tournament, ResetSetScore reset, DbUpdater batch) {
        final MatchInfo minfo = tournament.getMatchById(reset.getMid());
        final int numberOfSets = minfo.getNumberOfSets();
        if (numberOfSets < reset.getSetNumber()) {
            throw badRequest("Match has just " + numberOfSets + " sets");
        }
        if (numberOfSets == reset.getSetNumber()) {
            return;
        }
        if (minfo.getState() == Game) {
            truncateSets(batch, minfo, reset.getSetNumber());
            return;
        }
        final List<MatchInfo> groupMatches = minfo.getGid()
                .map(gid -> findMatchesInGroup(tournament, gid))
                .orElse(Lists.emptyList());

        boolean notAllMatchesComplete = !allMatchesInGroupComplete(groupMatches);
        final List<Integer> quitUids =
                tournament.getRule().getGroup().map(groupRules ->
                        groupService.orderUidsInGroup(tournament, groupMatches)
                                .subList(0, groupRules.getQuits())).orElse(emptyList());
        changeStatus(batch, minfo, Game);
        truncateSets(batch, minfo, reset.getSetNumber());
        if (!groupMatches.isEmpty() && notAllMatchesComplete) {
            return;
        }
        final List<MatchInfo> affectedMatches = findAffectedMatches(tournament, minfo, quitUids);
        resetMatches(tournament, batch, affectedMatches, quitUids);
        affectedMatches.stream().flatMap(m -> m.getParticipantIdScore().keySet().stream())
                .collect(toSet())
                .stream()
                .map(tournament::getParticipant)
                .forEach(participant -> bidService.setBidState(participant, Wait,
                        asList(Win1, Win2, Win3, Lost, Play), batch));
    }

    private void resetMatches(OpenTournamentMemState tournament, DbUpdater batch,
            List<MatchInfo> affectedMatches, List<Integer> quitUids) {
        affectedMatches.forEach(minfo ->
                quitUids.forEach(uid -> removeParticipant(tournament, batch, minfo, uid)));
    }

    private void removeParticipant(OpenTournamentMemState tournament,
            DbUpdater batch, MatchInfo minfo, int uid) {
        if (minfo.getParticipantIdScore().remove(uid) == null) {
            return;
        }
        matchDao.removeScores(batch, minfo.getMid(), uid);
        if (minfo.getParticipantIdScore().size() == 1) {
            final int opUid = minfo.getOpponentUid(uid).get();
            final BidState opoState = tournament.getParticipant(opUid).getState();
            switch (opoState) {
                case Expl:
                case Quit:
                    changeStatus(batch, minfo, Auto);
                    break;
                default:
                    changeStatus(batch, minfo, Draft);
                    break;
            }
            matchDao.removeSecondParticipant(batch, minfo.getMid(), opUid);
        } else if (minfo.getParticipantIdScore().isEmpty()) {
            changeStatus(batch, minfo, Draft);
            matchDao.removeParticipants(batch, minfo.getMid());
        }
    }

    private List<MatchInfo> findAffectedMatches(OpenTournamentMemState tournament,
            MatchInfo minfo, List<Integer> quitUids) {
        if (minfo.getGid().isPresent()) {
            return findPlayOffMatches(tournament, quitUids);
        } else {
            List<MatchInfo> winLoseMatches = getWinLoseMatches(tournament, minfo);
            List<MatchInfo> result = new ArrayList<>(winLoseMatches);
            findFollowingMatches(tournament, winLoseMatches, result);
            return result;
        }
    }

    private void findFollowingMatches(OpenTournamentMemState tournament,
            List<MatchInfo> baseMatches, List<MatchInfo> result) {
        if (baseMatches.isEmpty()) {
            return;
        }
        List<MatchInfo> follows = baseMatches.stream()
                .flatMap(m -> getWinLoseMatches(tournament, m).stream())
                .collect(toList());
        result.addAll(follows);
        findFollowingMatches(tournament, follows, result);
    }

    private List<MatchInfo> findPlayOffMatches(OpenTournamentMemState tournament, List<Integer> quitUids) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> quitUids.stream()
                        .anyMatch(uid -> minfo.getParticipantIdScore().containsKey(uid)))
                .collect(toList());
    }

    private List<MatchInfo> getWinLoseMatches(OpenTournamentMemState tournament, MatchInfo minfo) {
        final List<MatchInfo> result = new ArrayList<>();
        minfo.getWinnerMid().map(tournament::getMatchById).ifPresent(result::add);
        minfo.getLoserMid().map(tournament::getMatchById).ifPresent(result::add);
        return result;
    }

    private boolean allMatchesInGroupComplete(List<MatchInfo> groupMatches ) {
        return groupMatches.stream().allMatch(minfo -> minfo.getState() == Over);
    }

    private void truncateSets(DbUpdater batch, MatchInfo minfo, int setNumber) {
        matchDao.deleteSets(batch, minfo, setNumber);
        cutTrailingSets(minfo, setNumber);
    }

    public void cutTrailingSets(MatchInfo minfo, int setNumber) {
        minfo.getParticipantIdScore().values()
                .forEach(scores -> scores.subList(setNumber, scores.size()).clear());

    }
}
