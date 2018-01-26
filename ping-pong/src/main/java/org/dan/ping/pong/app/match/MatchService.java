package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
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
import static org.dan.ping.pong.app.match.MatchType.Gold;
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
import org.dan.ping.pong.app.playoff.PlayOffRule;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.sched.ScheduleService;
import org.dan.ping.pong.app.sched.TablesDiscovery;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.table.TableInfo;
import org.dan.ping.pong.app.tournament.ConfirmSetScore;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.SetScoreResultName;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentProgress;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.app.tournament.console.ConsoleStrategy;
import org.dan.ping.pong.app.user.UserRole;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.TriFunc;
import org.dan.ping.pong.util.time.Clocker;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class MatchService {
    private static final Comparator<MatchInfo> PARTICIPANT_MATCH_COMPARATOR = Comparator
            .comparing(MatchInfo::getState).reversed()
            .thenComparing(MatchInfo::getPriority)
            .thenComparing(MatchInfo::getMid);

    static final ImmutableSet<MatchState> EXPECTED_MATCH_STATES = ImmutableSet.of(Draft, Game, Place, Auto);
    private static final Set<BidState> PLAY_WAIT = ImmutableSet.of(Play, Wait);

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

    public static int roundPlayOffBase(int bids) {
        int places = 1;
        while (places < bids) {
            places *= 2;
        }
        return places;
    }

    @Inject
    @Named(SCHEDULE_SELECTOR)
    private ScheduleService scheduleService;

    @Inject
    private Sports sports;

    public SetScoreResult scoreSet(TournamentMemState tournament, Uid uid,
            SetScoreReq score, Instant now, DbUpdater batch) {
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

        final MatchInfo matchInfoForValidation = matchInfo.clone();
        matchInfoForValidation.addSetScore(score.getScores());
        sports.validateMatch(tournament, matchInfoForValidation);

        matchInfo.addSetScore(score.getScores());
        final Map<Uid, Integer> wonSets = sports.calcWonSets(tournament, matchInfo);
        final Optional<Uid> winUidO = sports.findWinnerId(tournament, wonSets);
        matchDao.scoreSet(tournament, matchInfo, batch, score.getScores());
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

    public void matchWinnerDetermined(TournamentMemState tournament, MatchInfo mInfo,
            Uid winUid, DbUpdater batch, Set<MatchState> completeMatchExStates) {
        completeMatch(mInfo, winUid, batch, completeMatchExStates);
        if (mInfo.getGid().isPresent()) {
            tryToCompleteGroup(tournament, mInfo, batch, PLAY_WAIT);
        } else {
            completePlayOffMatch(tournament, mInfo, winUid, batch);
        }
    }

    public void completeMatch(MatchInfo mInfo, Uid winUid,
            DbUpdater batch, Set<MatchState> expectedMatchStates) {
        log.info("Match {} won by {}", mInfo.getMid(), winUid);
        if (!expectedMatchStates.contains(mInfo.getState())) {
            throw internalError("Match " + mInfo.getMid()
                    + " should be in state " + expectedMatchStates);
        }
        mInfo.setState(Over);
        mInfo.setWinnerId(Optional.of(winUid));
        final Instant now = clocker.get();
        mInfo.setEndedAt(Optional.of(now));
        matchDao.completeMatch(mInfo.getMid(), winUid, now, batch, expectedMatchStates);
    }

    private BidState playOffMatchWinnerState(PlayOffRule playOff, MatchInfo mInfo,
            ParticipantMemState bid) {
        switch (mInfo.getType()) {
            case Gold:
                if (bid.getBidState() == Expl) {
                    return Expl;
                }
                return Win1;
            case Brnz:
                switch (playOff.getLosings()) {
                    case 1:
                        if (bid.getBidState() == Expl) {
                            return Expl;
                        }
                        return Win3;
                    case 2:
                        return Wait;
                    default:
                        throw internalError("unsupported number of losing " + playOff.getLosings());
                }
            case POff:
                return Wait;
            default:
                throw internalError("Match type " + mInfo.getType() + " is not supported");
        }
    }

    private BidState playOffMatchLoserState(PlayOffRule playOff, MatchInfo mInfo, ParticipantMemState bid) {
        switch (mInfo.getType()) {
            case Gold:
                if (bid.getBidState() == Expl) {
                    return Expl;
                }
                return Win2;
            case Brnz:
                switch (playOff.getLosings()) {
                    case 1:
                        return Lost;
                    case 2:
                        return Win3;
                    default:
                        throw internalError("unsupported number of losing " + playOff.getLosings());
                }
            case POff:
                return mInfo.getLoserMid().map(lMid -> Wait).orElse(Lost);
            default:
                throw internalError("Match type " + mInfo.getType() + " is not supported");
        }
    }

    private void completePlayOffMatch(TournamentMemState tournament, MatchInfo mInfo,
            Uid winUid, DbUpdater batch) {
        final PlayOffRule playOffRule = tournament.getRule().getPlayOff().get();
        final ParticipantMemState winBid = tournament.getBidOrQuit(winUid);
        if (!isPyrrhic(winBid)) {
            bidService.setBidState(winBid,
                    playOffMatchWinnerState(playOffRule, mInfo, winBid),
                    PLAY_WAIT, batch);
        }
        mInfo.getWinnerMid().ifPresent(wMid -> assignBidToMatch(tournament, wMid, winUid, batch));
        final Uid lostUid = mInfo.getOpponentUid(winUid)
                .orElseThrow(() -> internalError("no opponent in match " + mInfo.getMid()));
        final ParticipantMemState lostBid = tournament.getBidOrQuit(lostUid);
        if (!isPyrrhic(lostBid)) {
            bidService.setBidState(lostBid,
                    playOffMatchLoserState(playOffRule, mInfo, lostBid), PLAY_WAIT, batch);
        } else if (lostBid.getBidState() == Quit && mInfo.getType() == Gold) {
            bidService.setBidState(lostBid, Win2, singleton(lostBid.getBidState()), batch);
        }
        mInfo.getLoserMid().ifPresent(
                lMid -> assignBidToMatch(tournament, lMid, lostUid, batch));
        if (mInfo.getWinnerMid().isPresent()) {
            return;
        }
        tournamentService.endOfTournamentCategory(tournament, mInfo.getCid(), batch);
    }

    @Inject
    private TournamentService tournamentService;

    @Inject
    private GroupDao groupDao;

    public void tryToCompleteGroup(TournamentMemState tournament, MatchInfo matchInfo,
            DbUpdater batch, Collection<BidState> expected) {
        final int gid = matchInfo.getGid().get();
        final Set<Uid> matchUids = matchInfo.getParticipantIdScore().keySet();
        matchUids.stream()
                .map(tournament::getBidOrQuit)
                .filter(b -> expected.contains(b.getState()))
                .forEach(b -> bidService.setBidState(b,
                        completeGroupMatchBidState(tournament, b),
                        expected, batch));
        tryToCompleteGroup(tournament, gid, batch);
    }

    public void tryToCompleteGroup(TournamentMemState tournament, int gid, DbUpdater batch) {
        final Optional<List<MatchInfo>> oMatches = groupService
                .checkGroupComplete(tournament, gid);

        oMatches.ifPresent(matches ->
                completeParticipationLeftBids(
                        gid,
                        tournament,
                        completeGroup(gid, tournament, oMatches.get(), batch),
                        batch));
    }

    public BidState completeGroupMatchBidState(TournamentMemState tournament, ParticipantMemState b) {
        if (b.getGid().map(gid ->
                tournament.participantMatches(b.getUid())
                        .anyMatch(m -> m.getState() == Game && m.getGid().equals(b.getGid()))).orElse(false)) {
            return Play;
        }
        return Wait;
    }

    @Inject
    private ConsoleStrategy consoleStrategy;

    private void completeParticipationLeftBids(int gid, TournamentMemState tournament,
            Set<Uid> loserUids, DbUpdater batch) {
        consoleStrategy.onGroupComplete(gid, tournament, loserUids, batch);
    }

    @Inject
    private GroupService groupService;

    @Inject
    private BidService bidService;

    private void completeMiniTournamentGroup(
            TournamentMemState tournament, GroupInfo iGru,
            List<Uid> quitUids, DbUpdater batch) {
        if (iGru.getOrdNumber() == 0) {
            log.info("Set terminal bid states in mini tid {}", tournament.getTid());
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

    private Set<Uid> completeGroup(Integer gid, TournamentMemState tournament,
            List<MatchInfo> matches, DbUpdater batch) {
        log.info("Pick bids for playoff from gid {} in tid {}", gid, tournament.getTid());
        final int quits = tournament.getRule().getGroup().get().getQuits();
        final List<Uid> orderedUids = groupService.orderUidsInGroup(tournament, matches);
        final List<Uid> quitUids = selectQuitingUids(gid, tournament, orderedUids, quits);
        final GroupInfo iGru = tournament.getGroups().get(gid);
        final List<MatchInfo> playOffMatches = playOffMatches(tournament, iGru);
        if (playOffMatches.isEmpty()) {
            completeMiniTournamentGroup(tournament, iGru, quitUids, batch);
        } else {
            distributeGroupQuittersBetweenPlayOffMatches(tournament, batch, quits,
                    quitUids, iGru, playOffMatches);
        }
        return orderedUids.stream()
                .filter(uid -> !quitUids.contains(uid))
                .collect(toSet());
    }

    private List<MatchInfo> playOffMatches(TournamentMemState tournament, GroupInfo iGru) {
        return playOffService
                .findBaseMatches(tournament, iGru.getCid())
                .stream()
                .sorted(Comparator.comparing(MatchInfo::getMid))
                .collect(toList());
    }

    private List<Uid> selectQuitingUids(Integer gid, TournamentMemState tournament,
            List<Uid> orderUids, int quits) {
        final List<Uid> quitUids = orderUids.stream()
                .map(tournament::getBidOrExpl)
                .filter(b -> groupService.notExpelledInGroup(tournament, b))
                .limit(quits)
                .map(ParticipantMemState::getUid)
                .collect(toList());
        if (quitUids.size() < quits) {
            Stream.generate(() -> FILLER_LOSER_UID)
                    .limit(quits - quitUids.size())
                    .forEach(quitUids::add);
        }
        log.info("{} quit group {}", quitUids, gid);
        return quitUids;
    }

    private void distributeGroupQuittersBetweenPlayOffMatches(
            TournamentMemState tournament,
            DbUpdater batch, int quits, List<Uid> quitUids,
            GroupInfo iGru, List<MatchInfo> playOffMatches) {
        final List<GroupInfo> groups = tournament.getGroupsByCategory(iGru.getCid());
        final int expectedPlayOffMatches = roundPlayOffBase(quits * groups.size()) / 2;
        if (playOffMatches.size() != expectedPlayOffMatches) {
            throw internalError("Mismatch in number of play off matches "
                    + expectedPlayOffMatches + " !=" + playOffMatches.size()
                    + " group " + iGru.getGid());
        }
        final int roundedGroups = roundPlayOffBase(groups.size());
        final List<Integer> matchIndexes = PlayOffMatcherFromGroup.find(
                quits, iGru.getOrdNumber(), roundedGroups);
        for (int iQuitter = 0; iQuitter < quits; ++iQuitter) {
            int matchIdx = matchIndexes.get(iQuitter);
            assignBidToMatch(tournament, playOffMatches.get(matchIdx).getMid(),
                    quitUids.get(iQuitter), batch);
        }
    }

    private static final Set<BidState> quitOrExpl = ImmutableSet.of(Quit, Expl);

    public static boolean isPyrrhic(ParticipantMemState bid) {
        return bid.getUid().equals(FILLER_LOSER_UID)
                || quitOrExpl.contains(bid.getState());
    }

    private void nextMatch(MatchInfo mInfo, TournamentMemState tournament, DbUpdater batch, Uid uid) {
        if (mInfo.hasParticipant(uid)) {
            bidRewalksLadder(tournament, mInfo, batch, uid);
        } else {
            throw internalError("no participant "
                    + uid + " in mid " + mInfo.getMid());
        }
    }

    private void nextMatch(TournamentMemState tournament, Optional<Mid> nMid,
            MatchInfo mInfo, DbUpdater batch, Uid uid,
            TriFunc<PlayOffRule, MatchInfo, ParticipantMemState, BidState> stateF) {
        final ParticipantMemState bid = tournament.getBidOrQuit(uid);
        nMid.ifPresent(wMid -> nextMatch(tournament.getMatchById(wMid), tournament, batch, uid));
        if (!nMid.isPresent()) {
            bidService.setBidState(bid,
                    stateF.apply(tournament.getRule().getPlayOff().get(), mInfo, bid),
                    singleton(bid.getBidState()), batch);
            tournamentService.endOfTournamentCategory(tournament, bid.getCid(), batch);
        }
    }

    private void bidRewalksLadder(TournamentMemState tournament, MatchInfo mInfo, DbUpdater batch, Uid uid) {
        final ParticipantMemState bid = tournament.getBidOrQuit(uid);
        if (mInfo.getWinnerId().isPresent()) {
            if (mInfo.getWinnerId().equals(Optional.of(uid))) {
                nextMatch(tournament, mInfo.getWinnerMid(), mInfo, batch, uid, this::playOffMatchWinnerState);
            } else {
                nextMatch(tournament, mInfo.getLoserMid(), mInfo, batch, uid, this::playOffMatchLoserState);
            }
        } else {
            if (isPyrrhic(bid)) {
                mInfo.getLoserMid().ifPresent(
                        lMid -> nextMatch(tournament.getMatchById(lMid), tournament, batch, uid));
            } else {
                bidService.setBidState(bid, Wait, singletonList(bid.getBidState()), batch);
            }
        }
    }

    public void assignBidToMatch(TournamentMemState tournament, Mid mid, Uid uid, DbUpdater batch) {
        log.info("Assign uid {} to mid {} in tid {}", uid, mid, tournament.getTid());
        final MatchInfo mInfo = tournament.getMatchById(mid);
        if (mInfo.addParticipant(uid)) {
            log.info("Match is already complete and doesn't requires to be rescored");
            bidRewalksLadder(tournament, mInfo, batch, uid);
            return;
        }
        matchDao.setParticipant(mInfo.numberOfParticipants(), mInfo.getMid(), uid, batch);
        final int numberOfParticipants = mInfo.numberOfParticipants();
        if (numberOfParticipants == 2) {
            switch (mInfo.getState()) {
                case Draft:
                    final ParticipantMemState bid = tournament.getBidOrQuit(uid);
                    if (isPyrrhic(bid)) {
                        walkOver(tournament, uid, mInfo, batch);
                    } else if (bid.getState() == Wait) {
                        changeStatus(batch, mInfo, Place);
                    } else {
                        throw internalError("unexpected bid "
                                + uid + " state " + bid.getState());
                    }
                    break;
                case Auto:
                    autoWinComplete(tournament, mInfo, uid, batch);
                    break;
                default:
                    throw internalError("Unexpected state "
                            + mInfo.getState() + " in " + mid);
            }
        } else if (numberOfParticipants == 1) {
            final ParticipantMemState bid = tournament.getBidOrQuit(uid);
            if (isPyrrhic(bid)) {
                walkOver(tournament, uid, mInfo, batch);
            }
        } else {
            throw internalError("invalid number of participants " + numberOfParticipants);
        }
    }

    private void autoWinComplete(TournamentMemState tournament, MatchInfo mInfo,
            Uid winUid, DbUpdater batch) {
        log.info("Auto complete mid {} due {} quit and {} won",
                mInfo.getMid(), mInfo.getOpponentUid(winUid), winUid);
        completeMatch(mInfo, winUid, batch, singleton(Auto));
        switch (mInfo.getType()) {
            case Brnz:
                checkSinkMatch(mInfo);
                bidService.setBidState(tournament.getBidOrQuit(winUid), Win3, asList(Play, Wait, Rest), batch);
                tournamentService.endOfTournamentCategory(tournament, mInfo.getCid(), batch);
                break;
            case Gold:
                checkSinkMatch(mInfo);
                bidService.setBidState(tournament.getBidOrQuit(winUid), Win1, asList(Play, Wait, Rest), batch);
                tournamentService.endOfTournamentCategory(tournament, mInfo.getCid(), batch);
                break;
            case POff:
                mInfo.getWinnerMid()
                        .ifPresent(wMid -> assignBidToMatch(tournament, wMid, winUid, batch));
                // lMid branch is ready evaluated
                break;
            default:
                throw internalError("Unexpected state " + mInfo.getType());
        }
    }

    private void checkSinkMatch(MatchInfo mInfo) {
        mInfo.getWinnerMid().ifPresent(wMid -> {
            throw internalError("match " + mInfo.getMid() + " has a match for a winner");
        });
        mInfo.getLoserMid().ifPresent(wMid -> {
            throw internalError("match " + mInfo.getMid() + " has a match for a loser");
        });
    }

    public void changeStatus(DbUpdater batch, MatchInfo mInfo, MatchState state) {
        log.info("Change match {} status {} => {}",
                mInfo.getMid(), mInfo.getState(), state);
        if (mInfo.getState() == state) {
            return;
        }
        mInfo.setState(state);
        matchDao.changeStatus(mInfo.getMid(), state, batch);
    }

    private void checkPermissions(TournamentMemState tournament, Uid senderUid,
            MatchInfo matchInfo, SetScoreReq score) {
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
        if (Game != matchInfo.getState()) {
            throw badRequest("Match is not in a scorable state");
        }
    }

    public MatchScore matchScore(TournamentMemState tournament, MatchInfo matchInfo) {
        return MatchScore.builder()
                .mid(matchInfo.getMid())
                .tid(matchInfo.getTid())
                .winUid(matchInfo.getWinnerId())
                .sets(matchInfo.getParticipantIdScore())
                .wonSets(wonSets(tournament, matchInfo))
                .build();
    }

    private Map<Uid, Integer> wonSets(TournamentMemState tournament, MatchInfo matchInfo) {
        if (matchInfo.getParticipantIdScore().size() < 2) {
            return matchInfo.getParticipantIdScore().keySet()
                    .stream()
                    .collect(toMap(o -> o, o -> 0));
        }
        return sports.calcWonSets(tournament, matchInfo);
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

    public void walkOver(TournamentMemState tournament, Uid walkoverUid, MatchInfo mInfo, DbUpdater batch) {
        log.info("Uid {} walkovers mid {}", walkoverUid, mInfo.getMid());
        Optional<Uid> winUid = mInfo.getOpponentUid(walkoverUid);
        if (winUid.isPresent()) {
            matchWinnerDetermined(tournament, mInfo, winUid.get(), batch, EXPECTED_MATCH_STATES);
        } else {
            changeStatus(batch, mInfo, Auto);
            if (mInfo.getType() == Gold) {
                ParticipantMemState bid = tournament.getBidOrQuit(walkoverUid);
                if (bid.getBidState() == Quit) {
                    bidService.setBidState(bid, Win2, singleton(bid.getBidState()), batch);
                }
            }
            mInfo.getLoserMid()
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
                                .score(sports.calcWonSets(tournament, m).values().stream().collect(toList()))
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
                                .map(m -> {
                                    return MyPendingMatch.builder()
                                            .mid(m.getMid())
                                            .table(tablesDiscovery.discover(m.getMid()).map(TableInfo::toLink))
                                            .state(m.getState())
                                            .playedSets(m.getPlayedSets())
                                            .tid(tournament.getTid())
                                            .matchType(m.getType())
                                            .sport(tournament.getRule().getMatch().toMyPendingMatchSport())
                                            .enemy(m.getOpponentUid(uid).map(ouid ->
                                                    ofNullable(tournament.getBid(ouid))
                                                            .orElseThrow(() -> internalError("no opponent for "
                                                                    + uid + " in " + m)))
                                                    .map(ParticipantMemState::toLink))
                                            .build();
                                })
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
                .filter(m -> m.getState() == Over || m.getPlayedSets() > 0)
                .sorted(Comparator.comparing(m -> m.getEndedAt().orElse(Instant.MIN)))
                .collect(toList());
        return PlayedMatchList.builder()
                .participant(tournament.getParticipant(uid).toLink())
                .progress(tournamentProgress(tournament, uid))
                .inGroup(completeMatches.stream().filter(m -> m.getType() == Grup)
                        .map(m -> PlayedMatchLink.builder()
                                .mid(m.getMid())
                                .opponent(m.getOpponentUid(uid)
                                        .map(tournament::getBidOrExpl)
                                        .map(ParticipantMemState::toLink)
                                        .get())
                                .winnerUid(m.getWinnerId())
                                .build())
                        .collect(toList()))
                .playOff(completeMatches.stream().filter(m -> m.getType() != Grup)
                        .map(m -> PlayedMatchLink.builder()
                                .mid(m.getMid())
                                .opponent(m.getOpponentUid(uid)
                                        .map(tournament::getBidOrExpl)
                                        .map(ParticipantMemState::toLink)
                                        .get())
                                .winnerUid(m.getWinnerId())
                                .build())
                        .collect(toList()))
                .build();
    }

    public MatchResult matchResult(TournamentMemState tournament, Mid mid, Optional<Uid> ouid) {
        final MatchInfo m = tournament.getMatchById(mid);
        return MatchResult.builder()
                .participants(m.getParticipantIdScore().keySet().stream()
                        .map(uid -> tournament.getBidOrExpl(uid).toLink())
                        .collect(toList()))
                .score(matchScore(tournament, m))
                .role(detectRole(tournament, m, ouid))
                .state(m.getState())
                .type(m.getType())
                .group(m.getGid().map(gid -> tournament.getGroups().get(gid).toLink()))
                .category(tournament.getCategory(m.getCid()))
                .tid(tournament.getTid())
                .playedSets(m.getPlayedSets())
                .sport(tournament.getRule().getMatch().toMyPendingMatchSport())
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
                .sport(tournament.getRule().getMatch().toMyPendingMatchSport())
                .playedSets(m.getPlayedSets())
                .started(m.getStartedAt())
                .matchType(m.getType())
                .table(tablesDiscovery.discover(m.getMid()).map(TableInfo::toLink))
                .participants(m.getParticipantIdScore().keySet().stream()
                        .map(uid -> tournament.getBidOrExpl(uid).toLink())
                        .collect(toList()))
                .build();
    }

    public List<Mid> findMatchesByParticipants(TournamentMemState tournament,
            Uid uid1, Uid uid2) {
        return tournament.getMatches().values().stream()
                .filter(m -> m.hasParticipant(uid1) && m.hasParticipant(uid2))
                .map(MatchInfo::getMid)
                .collect(toList());
    }
}
