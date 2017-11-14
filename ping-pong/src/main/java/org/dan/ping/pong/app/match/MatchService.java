package org.dan.ping.pong.app.match;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
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
import static org.dan.ping.pong.app.match.MatchType.Grup;
import static org.dan.ping.pong.app.place.ArenaDistributionPolicy.NO;
import static org.dan.ping.pong.app.sched.ScheduleCtx.SCHEDULE_SELECTOR;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_UID;
import static org.dan.ping.pong.app.tournament.SetScoreResultName.MatchContinues;
import static org.dan.ping.pong.app.user.UserRole.Admin;
import static org.dan.ping.pong.app.user.UserRole.Participant;
import static org.dan.ping.pong.app.user.UserRole.Spectator;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.group.PlayOffMatcherFromGroup;
import org.dan.ping.pong.app.place.PlaceRules;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.sched.ScheduleService;
import org.dan.ping.pong.app.sched.TablesDiscovery;
import org.dan.ping.pong.app.table.TableInfo;
import org.dan.ping.pong.app.tournament.ConfirmSetScore;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.SetScoreResultName;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentProgress;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.app.user.UserRole;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class MatchService {
    private static final Comparator<MatchInfo> PARTICIPANT_MATCH_COMPARATOR = Comparator
            .comparing(MatchInfo::getState).reversed()
            .thenComparing(MatchInfo::getPriority)
            .thenComparing(MatchInfo::getMid);

    static final ImmutableSet<MatchState> EXPECTED_MATCH_STATES = ImmutableSet.of(Game, Place, Auto);

    private static Comparator<MatchInfo> noTablesParticipantMatchComparator(
            TournamentMemState tournament, Uid uid) {
        return Comparator
                .comparing(MatchInfo::getState).reversed()
                .thenComparing(m -> m.getOpponentUid(uid)
                        .map(ouid -> tournament.getParticipant(ouid).getName())
                        .orElse(""))
                .thenComparing(MatchInfo::getMid);
    }

    @Inject
    private MatchDao matchDao;

    @Inject
    private Clocker clocker;

    public int roundPlayOffBase(int bids) {
        int places = 1;
        while (places < bids) {
            places *= 2;
        }
        return places;
    }

    @Inject
    @Named(SCHEDULE_SELECTOR)
    private ScheduleService scheduleService;

    public SetScoreResult scoreSet(TournamentMemState tournament, Uid uid,
            FinalMatchScore score, Instant now, DbUpdater batch) {
        tournament.getRule().getMatch().validateSet(score.getSetOrdNumber(), score.getScores());
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
        final Optional<Uid> winUidO = matchDao.scoreSet(tournament, matchInfo, batch, score.getScores());
        winUidO.ifPresent(winUid -> matchWinnerDetermined(tournament, matchInfo, winUid, batch, EXPECTED_MATCH_STATES));

        scheduleService.afterMatchComplete(tournament, batch, now);
        return setScoreResult(tournament,
                winUidO.map(u -> tournament.matchScoreResult())
                        .orElse(SetScoreResultName.MatchContinues),
                matchInfo);
    }

    SetScoreResult setScoreResult(TournamentMemState tournament, SetScoreResultName name, MatchInfo matchInfo) {
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
                        .nextSetNumberToScore(Optional.of(matchInfo.getPlayedSets()))
                        .build();
            default:
                throw internalError("Unknown state " + name);
        }
    }

    public void matchWinnerDetermined(TournamentMemState tournament, MatchInfo matchInfo,
            Uid winUid, DbUpdater batch, Set<MatchState> completeMatchExStates) {
        completeMatch(matchInfo, winUid, batch, completeMatchExStates);
        if (matchInfo.getGid().isPresent()) {
            tryToCompleteGroup(tournament, matchInfo, batch, singletonList(Play));
        } else {
            completePlayOffMatch(tournament, matchInfo, winUid, batch);
        }
    }

    private void completeMatch(MatchInfo matchInfo, Uid winUid,
            DbUpdater batch, Set<MatchState> expectedMatchStates) {
        matchInfo.setState(Over);
        matchInfo.setWinnerId(Optional.of(winUid));
        final Instant now = clocker.get();
        matchInfo.setEndedAt(Optional.of(now));
        matchDao.completeMatch(matchInfo.getMid(), winUid, now, batch, expectedMatchStates);
    }

    private void completeNoLastPlayOffMatch(TournamentMemState tournament, MatchInfo matchInfo,
            Uid winUid, DbUpdater batch) {
        ParticipantMemState winBid = tournament.getParticipants().get(winUid);
        bidService.setBidState(winBid, Wait, singletonList(Play), batch);
        assignBidToMatch(tournament, matchInfo.getWinnerMid().get(), winUid, batch);
        final Uid lostUid = matchInfo.getOpponentUid(winUid).get();
        final ParticipantMemState lostBid = tournament.getParticipants().get(lostUid);
        if (lostBid == null) {
            checkArgument(FILLER_LOSER_UID.equals(lostUid));
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

    private void completeLastPlayOffMatch(TournamentMemState tournament, MatchInfo matchInfo,
            Uid winUid, DbUpdater batch) {
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

    private void setMedalMatchBidStatuses(
            TournamentMemState tournament,
            MatchInfo matchInfo,
            Uid winUid, DbUpdater batch,
            BidState win, BidState lost) {
        matchInfo.getParticipantIdScore().keySet().forEach(uid ->
                bidService.setBidState(tournament.getParticipant(uid),
                        winUid.equals(uid) ? win : lost, asList(Play, Rest), batch));
    }

    private void completePlayOffMatch(TournamentMemState tournament, MatchInfo matchInfo,
            Uid winUid, DbUpdater batch) {
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

    public void tryToCompleteGroup(TournamentMemState tournament, MatchInfo matchInfo,
            DbUpdater batch, List<BidState> expected) {
        final int gid = matchInfo.getGid().get();
        final Set<Uid> uids = matchInfo.getParticipantIdScore().keySet();
        final List<MatchInfo> matches = groupService.findMatchesInGroup(tournament, gid);
        final long completedMatches = matches.stream()
                .map(MatchInfo::getState)
                .filter(Over::equals)
                .count();
        uids.stream().map(tournament::getBid).filter(b -> b.getState() == Wait)
                .forEach(b -> bidService.setBidState(b, Wait, expected, batch));
        if (completedMatches < matches.size()) {
            log.debug("Matches {} left to play in the group {}",
                    matches.size() - completedMatches, gid);
            return;
        }
        completeParticipationLeftBids(
                tournament,
                completeGroup(gid, tournament, matches, batch),
                batch);
    }

    private void completeParticipationLeftBids(TournamentMemState tournament,
            List<Uid> leftUids, DbUpdater batch) {
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
            TournamentMemState tournament, GroupInfo iGru,
            List<Uid> quitUids, DbUpdater batch) {
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

    private List<Uid> completeGroup(Integer gid, TournamentMemState tournament,
            List<MatchInfo> matches, DbUpdater batch) {
        log.info("Pick bids for playoff from gid {} in tid {}", gid, tournament.getTid());
        final int quits = tournament.getRule().getGroup().get().getQuits();
        final List<Uid> orderUids = groupService.orderUidsInGroup(tournament, matches);
        final List<Uid> quitUids = orderUids.subList(0, quits);
        log.info("{} quit group {}", quitUids, gid);
        final GroupInfo iGru = tournament.getGroups().get(gid);
        final List<MatchInfo> playOffMatches = playOffService
                .findBaseMatches(tournament, iGru.getCid())
                .stream()
                .sorted(Comparator.comparing(MatchInfo::getMid))
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

    public void assignBidToMatch(TournamentMemState tournament, Mid mid, Uid uid, DbUpdater batch) {
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

    private void autoWinComplete(TournamentMemState tournament, MatchInfo matchInfo,
            Uid uid, DbUpdater batch) {
        log.info("Auto complete mid {} due {} quit and {} detected",
                matchInfo.getMid(), matchInfo.getOpponentUid(uid), uid);
        completeMatch(matchInfo, uid, batch, ImmutableSet.of(Game, Place, Auto));
        matchInfo.getWinnerMid()
                .ifPresent(wmid -> assignBidToMatch(tournament, wmid, uid, batch));
        if (FILLER_LOSER_UID.equals(uid)) {
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

    public void changeStatus(DbUpdater batch, MatchInfo matchInfo, MatchState state) {
        if (matchInfo.getState() == state) {
            return;
        }
        matchInfo.setState(state);
        matchDao.changeStatus(matchInfo.getMid(), state, batch);
    }

    private void checkPermissions(TournamentMemState tournament, Uid senderUid,
            MatchInfo matchInfo, FinalMatchScore score) {
        final Set<Uid> scoreUids = score.getScores().stream()
                .map(IdentifiedScore::getUid).collect(toSet());
        final Set<Uid> participantIds = matchInfo.getParticipantIdScore().keySet();
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
        final Map<Uid, Integer> newSetScore = score.getScores().stream()
                .collect(toMap(IdentifiedScore::getUid, IdentifiedScore::getScore));
        final int playedSets = matchInfo.getPlayedSets();
        if (playedSets < score.getSetOrdNumber()) {
            throw badRequest("Set " + playedSets + " needs to be scored first");
        }
        if (playedSets > score.getSetOrdNumber()) {
            final Map<Uid, Integer> setScore = matchInfo.getSetScore(score.getSetOrdNumber());
            if (newSetScore.equals(setScore)) {
                throw new ConfirmSetScore();
            }
            throw badRequest(new MatchScoredError(matchScore(tournament, matchInfo)));
        }
    }

    public MatchScore matchScore(TournamentMemState tournament, MatchInfo matchInfo) {
        return MatchScore.builder()
                .mid(matchInfo.getMid())
                .tid(matchInfo.getTid())
                .winUid(matchInfo.getWinnerId())
                .sets(matchInfo.getParticipantIdScore())
                .wonSets(tournament.getRule().getMatch()
                        .calcWonSets(matchInfo.getParticipantIdScore()))
                .build();
    }

    public static final Set<MatchState> incompleteMatchStates = ImmutableSet.of(Draft, Place, Game);

    public List<MatchInfo> bidIncompleteGroupMatches(Uid uid, TournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getParticipantIdScore().containsKey(uid))
                .filter(minfo -> minfo.getGid().isPresent())
                .filter(minfo -> incompleteMatchStates.contains(minfo.getState()))
                .collect(toList());
    }

    public void leaveFromPlayOff(ParticipantMemState bid, TournamentMemState tournament, DbUpdater batch) {
        playOffMatchForResign(bid.getUid(), tournament)
                .ifPresent(match -> {
                    walkOver(tournament, bid.getUid(), match, batch);
                    leaveFromPlayOff(bid, tournament, batch);
                });
    }

    public void walkOver(TournamentMemState tournament, Uid walkoverUid, MatchInfo matchInfo, DbUpdater batch) {
        log.info("Uid {} walkovers mid {}", walkoverUid, matchInfo.getMid());
        Optional<Uid> winUid = matchInfo.getOpponentUid(walkoverUid);
        if (winUid.isPresent()) {
            matchWinnerDetermined(tournament, matchInfo, winUid.get(), batch, EXPECTED_MATCH_STATES);
        } else {
            changeStatus(batch, matchInfo, Auto);
            matchInfo.getLoserMid()
                    .ifPresent(lmid -> assignBidToMatch(tournament, lmid, walkoverUid, batch));
        }
    }

    public Optional<MatchInfo> playOffMatchForResign(Uid uid, TournamentMemState tournament) {
        return tournament.getMatches().values()
                .stream()
                .filter(minfo -> minfo.hasParticipant(uid))
                .filter(minfo -> !minfo.getGid().isPresent())
                .filter(minfo -> incompleteMatchStates.contains(minfo.getState()))
                .findAny();
    }

    public List<OpenMatchForWatch> findOpenMatchesForWatching(TournamentMemState tournament) {
        return scheduleService.withPlaceTables(tournament,
                tablesDiscovery -> tournament.getMatches().values().stream()
                        .filter(m -> m.getState() == Game)
                        .map(m -> OpenMatchForWatch.builder()
                                .mid(m.getMid())
                                .started(m.getStartedAt().get())
                                .score(tournament.getRule().getMatch()
                                        .calcWonSets(m.getParticipantIdScore())
                                        .values()
                                        .stream().collect(toList()))
                                .category(tournament.getCategory(m.getCid()))
                                .table(tablesDiscovery.discover(m.getMid()).map(TableInfo::toLink))
                                .type(m.getType())
                                .participants(
                                        m.getUids().stream()
                                                .map(tournament::getParticipant)
                                                .map(ParticipantMemState::toLink)
                                                .collect(toList()))
                                .build())
                        .collect(toList()));
    }

    public MyPendingMatchList findPendingMatches(
            TournamentMemState tournament, Uid uid) {
        return scheduleService.withPlaceTables(tournament, tablesDiscovery ->
                MyPendingMatchList.builder()
                        .matches(tournament.participantMatches(uid)
                                .filter(m -> incompleteMatchStates.contains(m.getState()))
                                .sorted(tournament.getRule().getPlace()
                                        .map(PlaceRules::getArenaDistribution).orElse(NO) == NO
                                        ? noTablesParticipantMatchComparator(tournament, uid)
                                        : PARTICIPANT_MATCH_COMPARATOR)
                                .map(m -> MyPendingMatch.builder()
                                        .mid(m.getMid())
                                        .table(tablesDiscovery.discover(m.getMid()).map(TableInfo::toLink))
                                        .state(m.getState())
                                        .playedSets(m.getPlayedSets())
                                        .tid(tournament.getTid())
                                        .matchType(m.getType())
                                        .minGamesToWin(tournament.getRule().getMatch().getMinGamesToWin())
                                        .enemy(m.getOpponentUid(uid).map(ouid ->
                                                ofNullable(tournament.getBid(ouid))
                                                        .orElseThrow(() -> internalError("no opponent for "
                                                        + uid + " in " + m)))
                                                .map(ParticipantMemState::toLink))
                                        .build())
                                .collect(toList()))
                        .showTables(tournament.getRule().getPlace().map(PlaceRules::getArenaDistribution)
                                .orElse(NO) != NO)
                        .progress(tournamentProgress(tournament, uid))
                        .bidState(tournament.getParticipant(uid).getState())
                        .build());
    }

    private TournamentProgress tournamentProgress(TournamentMemState tournament, Uid uid) {
        return TournamentProgress.builder()
                .leftMatches(tournament.participantMatches(uid)
                        .map(MatchInfo::getState)
                        .filter(incompleteMatchStates::contains)
                        .count())
                .totalMatches(tournament.participantMatches(uid).count())
                .build();
    }

    private TournamentProgress tournamentProgress(TournamentMemState tournament) {
        return TournamentProgress.builder()
                .leftMatches(tournament.getMatches().values().stream()
                        .map(MatchInfo::getState)
                        .filter(incompleteMatchStates::contains)
                        .count())
                .totalMatches(tournament.getMatches().size())
                .build();
    }

    public List<OpenMatchForJudge> findOpenMatchesFurJudge(TournamentMemState tournament) {
        return scheduleService.withPlaceTables(tournament, (tablesDiscovery -> tournament.getMatches()
                .values().stream()
                .filter(m -> m.getState() == Game)
                .map(m -> getMatchForJudge(tournament, m, tablesDiscovery))
                .collect(toList())));
    }

    public OpenMatchForJudgeList findOpenMatchesForJudgeList(TournamentMemState tournament) {
        return OpenMatchForJudgeList.builder()
                .matches(findOpenMatchesFurJudge(tournament))
                .progress(tournamentProgress(tournament))
                .build();
    }

    public PlayedMatchList findPlayedMatchesByBid(TournamentMemState tournament, Uid uid) {
        final List<MatchInfo> completeMatches = tournament.participantMatches(uid)
                .filter(m -> m.getState() == Over)
                .sorted(Comparator.comparing(m -> m.getEndedAt().get()))
                .collect(toList());
        return PlayedMatchList.builder()
                .participant(tournament.getParticipant(uid).toLink())
                .progress(tournamentProgress(tournament, uid))
                .inGroup(completeMatches.stream().filter(m -> m.getType() == Grup)
                        .map(m -> PlayedMatchLink.builder()
                                .mid(m.getMid())
                                .opponent(m.getOpponentUid(uid)
                                        .map(tournament::getParticipant)
                                        .map(ParticipantMemState::toLink)
                                        .get())
                                .winnerUid(m.getWinnerId().orElseThrow(() -> internalError("no winner")))
                                .build())
                        .collect(toList()))
                .playOff(completeMatches.stream().filter(m -> m.getType() != Grup)
                        .map(m -> PlayedMatchLink.builder()
                                .mid(m.getMid())
                                .opponent(m.getOpponentUid(uid)
                                        .map(tournament::getParticipant)
                                        .map(ParticipantMemState::toLink)
                                        .get())
                                .winnerUid(m.getWinnerId().orElseThrow(() -> internalError("no winner")))
                                .build())
                        .collect(toList()))
                .build();
    }

    public MatchResult matchResult(TournamentMemState tournament, Mid mid, Optional<Uid> ouid) {
        MatchInfo m = tournament.getMatchById(mid);
        return MatchResult.builder()
                .participants(m.getParticipantIdScore().keySet().stream()
                        .map(uid -> tournament.getParticipant(uid).toLink())
                        .collect(toList()))
                .score(matchScore(tournament, m))
                .role(detectRole(tournament, m, ouid))
                .state(m.getState())
                .type(m.getType())
                .group(m.getGid().map(gid -> tournament.getGroups().get(gid).toLink()))
                .category(tournament.getCategory(m.getCid()))
                .tid(tournament.getTid())
                .playedSets(m.getPlayedSets())
                .minGamesToWin(tournament.getRule().getMatch().getMinGamesToWin())
                .build();
    }

    public UserRole detectRole(TournamentMemState tournament, MatchInfo match, Optional<Uid> ouid) {
        return ouid.map(uid -> {
            if (tournament.isAdminOf(uid)) {
                return Admin;
            } else if (match.getParticipantIdScore().containsKey(uid)) {
                return Participant;
            }
            return Spectator;
        }).orElse(Spectator);
    }

    public OpenMatchForJudge getMatchForJudge(TournamentMemState tournament, Mid mid) {
        return scheduleService.withPlaceTables(tournament,
                tablesDiscovery -> getMatchForJudge(tournament,
                        tournament.getMatchById(mid), tablesDiscovery));
    }

    private OpenMatchForJudge getMatchForJudge(
            TournamentMemState tournament, MatchInfo m,
            TablesDiscovery tablesDiscovery) {
        return OpenMatchForJudge.builder()
                .mid(m.getMid())
                .tid(tournament.getTid())
                .minGamesToWin(tournament.getRule().getMatch().getMinGamesToWin())
                .playedSets(m.getPlayedSets())
                .started(m.getStartedAt().get())
                .matchType(m.getType())
                .table(tablesDiscovery.discover(m.getMid()).map(TableInfo::toLink))
                .participants(m.getParticipantIdScore().keySet().stream()
                        .map(uid -> tournament.getParticipant(uid).toLink())
                        .collect(toList()))
                .build();
    }
}
