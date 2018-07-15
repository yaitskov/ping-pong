package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
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
import static org.dan.ping.pong.app.match.MatchResource.UID;
import static org.dan.ping.pong.app.match.MatchState.Auto;
import static org.dan.ping.pong.app.match.MatchState.Draft;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.INCOMPLETE_MATCH_STATES;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.match.MatchType.Grup;
import static org.dan.ping.pong.app.place.ArenaDistributionPolicy.NO;
import static org.dan.ping.pong.app.sched.ScheduleCtx.SCHEDULE_SELECTOR;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;
import static org.dan.ping.pong.app.tournament.SetScoreResultName.MatchContinues;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.app.user.UserRole.Admin;
import static org.dan.ping.pong.app.user.UserRole.Participant;
import static org.dan.ping.pong.app.user.UserRole.Spectator;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.group.PlayOffMatcherFromGroup;
import org.dan.ping.pong.app.place.PlaceRules;
import org.dan.ping.pong.app.playoff.PlayOffRule;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.sched.ScheduleService;
import org.dan.ping.pong.app.sched.TablesDiscovery;
import org.dan.ping.pong.app.sport.MatchRules;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.table.TableInfo;
import org.dan.ping.pong.app.tournament.ChildTournamentProvider;
import org.dan.ping.pong.app.tournament.ConfirmSetScore;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.SetScoreResultName;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentCache;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentProgress;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.app.tournament.TournamentTerminator;
import org.dan.ping.pong.app.tournament.console.ConsoleStrategy;
import org.dan.ping.pong.app.user.UserRole;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.TriFunc;
import org.dan.ping.pong.util.time.Clocker;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
            TournamentMemState tournament, Bid bid) {
        return Comparator
                .comparing(MatchInfo::getState).reversed()
                .thenComparing(m -> m.getOpponentBid(bid)
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

    public Optional<Bid> resolveUidAndMidToBid(TournamentMemState tournament,
            Uid uid, MatchInfo mInfo) {
        return ofNullable(tournament.getUidCid2Bid().get(uid))
                .flatMap(m -> ofNullable(m.get(mInfo.getCid())));
    }

    public SetScoreResult scoreSet(TournamentMemState tournament, Uid uid,
            SetScoreReq score, Instant now, DbUpdater batch) {
        final MatchInfo matchInfo = tournament.getMatchById(score.getMid());
        try {
            checkPermissions(tournament, uid,
                    resolveUidAndMidToBid(tournament, uid, matchInfo), matchInfo, score);
        } catch (ConfirmSetScore e) {
            log.info("User {} confirmed set score in mid {}", uid, score.getMid());
            return setScoreResult(tournament,
                    matchInfo.getState() == Over
                            ? tournament.matchScoreResult()
                            : MatchContinues,
                    matchInfo);
        }

        final List<SetScoreReq> scoredSets = expandScoreSet(
                score, tournament.selectMatchRule(matchInfo));
        Optional<Bid> winBidO = empty();
        for (SetScoreReq atomicSetScore : scoredSets) {
            winBidO.ifPresent((winUid) -> {
                throw internalError("preliminary match winner", UID, winUid);
            });
            winBidO = applyAtomicSetScore(tournament, atomicSetScore, now, batch, matchInfo);
        }
        return setScoreResult(tournament,
                winBidO.map(u -> tournament.matchScoreResult())
                        .orElse(SetScoreResultName.MatchContinues),
                matchInfo);
    }

    private List<SetScoreReq> expandScoreSet(SetScoreReq score, MatchRules matchRules) {
        if (matchRules.countOnlySets()) {
            return sports.expandScoreSet(matchRules, score);
        } else {
            return singletonList(score);
        }
    }

    private Optional<Bid> applyAtomicSetScore(TournamentMemState tournament, SetScoreReq score,
            Instant now, DbUpdater batch, MatchInfo matchInfo) {
        final MatchInfo matchInfoForValidation = matchInfo.clone();
        matchInfoForValidation.addSetScore(score.getScores());
        sports.validateMatch(tournament, matchInfoForValidation);

        matchInfo.addSetScore(score.getScores());
        final Map<Bid, Integer> wonSets = sports.calcWonSets(tournament, matchInfo);
        final Optional<Bid> winUidO = sports.findWinnerId(
                tournament.selectMatchRule(matchInfo), wonSets);
        matchDao.scoreSet(tournament, matchInfo, batch, score.getScores());
        winUidO.ifPresent(winUid -> matchWinnerDetermined(tournament, matchInfo,
                winUid, batch, EXPECTED_MATCH_STATES));
        scheduleService.afterMatchComplete(tournament, batch, now);
        return winUidO;
    }

    SetScoreResult setScoreResult(
            TournamentMemState tournament, SetScoreResultName name, MatchInfo matchInfo) {
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
                        .nextSetNumberToScore(Optional.of(matchInfo.playedSets()))
                        .build();
            default:
                throw internalError("Unknown state " + name);
        }
    }

    public void matchWinnerDetermined(TournamentMemState tournament, MatchInfo mInfo,
            Bid winBid, DbUpdater batch, Set<MatchState> completeMatchExStates) {
        completeMatch(mInfo, winBid, batch, completeMatchExStates);
        if (mInfo.getGid().isPresent()) {
            tryToCompleteGroup(tournament, mInfo, batch, PLAY_WAIT);
        } else {
            completePlayOffMatch(tournament, mInfo, winBid, batch);
        }
    }

    public void completeMatch(MatchInfo mInfo, Bid winBid,
            DbUpdater batch, Set<MatchState> expectedMatchStates) {
        log.info("Match {} won by {}", mInfo.getMid(), winBid);
        if (!expectedMatchStates.contains(mInfo.getState())) {
            throw internalError("Match " + mInfo.getMid()
                    + " should be in state " + expectedMatchStates);
        }
        mInfo.setState(Over);
        mInfo.setWinnerId(Optional.of(winBid));
        final Instant now = clocker.get();
        mInfo.setEndedAt(Optional.of(now));
        matchDao.completeMatch(mInfo, batch, expectedMatchStates);
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

    @Inject
    private TournamentTerminator tournamentTerminator;

    private static Set<BidState> BID_STATES_ALLOWED_IN_CONSOLE = ImmutableSet.of(Lost);

    private void completePlayOffMatch(TournamentMemState tournament, MatchInfo mInfo,
            Bid winBid, DbUpdater batch) {
        final PlayOffRule playOffRule = tournament.getRule().getPlayOff().get();
        final ParticipantMemState winParticipant = tournament.getBidOrQuit(winBid);
        if (!isPyrrhic(winParticipant)) {
            bidService.setBidState(winParticipant,
                    playOffMatchWinnerState(playOffRule, mInfo, winParticipant),
                    PLAY_WAIT, batch);
        }
        mInfo.getWinnerMid().ifPresent(wMid -> assignBidToMatch(tournament, wMid, winBid, batch));
        final Bid lostBid = mInfo.getOpponentBid(winBid)
                .orElseThrow(() -> internalError("no opponent in match " + mInfo.getMid()));
        final ParticipantMemState lostParticipant = tournament.getBidOrQuit(lostBid);
        if (!isPyrrhic(lostParticipant)) {
            bidService.setBidState(lostParticipant,
                    playOffMatchLoserState(playOffRule, mInfo, lostParticipant), PLAY_WAIT, batch);
            if (BID_STATES_ALLOWED_IN_CONSOLE.contains(lostParticipant.getBidState())) {
                consoleStrategy.onParticipantLostPlayOff(tournament, lostParticipant, batch);
            }
        } else if (lostParticipant.getBidState() == Quit && mInfo.getType() == Gold) {
            bidService.setBidState(lostParticipant, Win2,
                    singleton(lostParticipant.getBidState()), batch);
        }
        mInfo.getLoserMid().ifPresent(
                lMid -> assignBidToMatch(tournament, lMid, lostBid, batch));
        if (mInfo.getWinnerMid().isPresent()) {
            return;
        }
        tournamentTerminator.endOfTournamentCategory(tournament, mInfo.getCid(), batch);
    }

    @Inject
    private TournamentService tournamentService;

    @Inject
    private GroupDao groupDao;

    public void tryToCompleteGroup(TournamentMemState tournament, MatchInfo matchInfo,
            DbUpdater batch, Collection<BidState> expected) {
        final Gid gid = matchInfo.getGid().get();
        final Set<Bid> matchUids = matchInfo.getParticipantIdScore().keySet();
        matchUids.stream()
                .map(tournament::getBidOrQuit)
                .filter(b -> expected.contains(b.state()))
                .forEach(b -> bidService.setBidState(b,
                        completeGroupMatchBidState(tournament, b),
                        expected, batch));
        tryToCompleteGroup(tournament, gid, batch);
    }

    public void tryToCompleteGroup(TournamentMemState tournament, Gid gid, DbUpdater batch) {
        final Optional<List<MatchInfo>> oMatches = groupService
                .checkGroupComplete(tournament, gid);

        try {
            oMatches.ifPresent(matches ->
                    completeParticipationLeftBids(
                            gid,
                            tournament,
                            completeGroup(gid, tournament, oMatches.get(), batch),
                            batch));
        } catch (NoDisambiguateMatchesException e) {
            groupService.createDisambiguateMatches(batch, e, tournament,
                    oMatches.orElseThrow(() -> internalError("No matches in group")));
        }
    }

    public BidState completeGroupMatchBidState(
            TournamentMemState tournament,
            ParticipantMemState b) {
        if (b.getGid().map(gid ->
                tournament.participantMatches(b.getBid())
                        .anyMatch(m -> m.getState() == Game
                                && m.getGid().equals(b.getGid()))).orElse(false)) {
            return Play;
        }
        return Wait;
    }

    @Inject
    private ConsoleStrategy consoleStrategy;

    private void completeParticipationLeftBids(Gid gid, TournamentMemState tournament,
            Set<Bid> loserBids, DbUpdater batch) {
        consoleStrategy.onGroupComplete(gid, tournament, loserBids, batch);
    }

    @Inject
    private GroupService groupService;

    @Inject
    private BidService bidService;

    private void completeMiniTournamentGroup(
            TournamentMemState tournament, GroupInfo iGru,
            List<Bid> quitBids, DbUpdater batch) {
        if (tournament.findGroupsByCategory(iGru.getCid()).count() == 1) {
            log.info("Set terminal bid states in mini tid {}", tournament.getTid());
            for (int i = 0; i < quitBids.size(); ++i) {
                bidService.setBidState(tournament.getBidOrQuit(quitBids.get(i)),
                        i < WIN_STATES.size()
                                ? WIN_STATES.get(i)
                                : Lost,
                        asList(Play, Wait, Quit, Lost, Rest), batch);
            }
            tournamentTerminator.endOfTournamentCategory(tournament, iGru.getCid(), batch);
        } else {
            throw internalError("Tournament " +
                    tournament.getTid() + " in category " + iGru.getCid()
                    + " doesn't have playoff matches");
        }
    }

    @Inject
    private PlayOffService playOffService;

    private Set<Bid> completeGroup(Gid gid, TournamentMemState tournament,
            List<MatchInfo> matches, DbUpdater batch) {
        log.info("Pick bids for playoff from gid {} in tid {}", gid, tournament.getTid());
        final int quits = tournament.getRule().getGroup().get().getQuits();
        final List<Bid> orderedUids = groupService.orderBidsInGroup(gid, tournament, matches);
        final List<Bid> quitBids = selectQuitingUids(gid, tournament, orderedUids, quits);
        final GroupInfo iGru = tournament.getGroups().get(gid);
        final List<MatchInfo> playOffMatches = playOffMatches(tournament, iGru, empty());
        if (playOffMatches.isEmpty()) {
            completeMiniTournamentGroup(tournament, iGru, quitBids, batch);
        } else {
            distributeGroupQuittersBetweenPlayOffMatches(tournament, batch, quits,
                    quitBids, iGru, playOffMatches);
        }
        return orderedUids.stream()
                .filter(uid -> !quitBids.contains(uid))
                .collect(toSet());
    }

    private List<MatchInfo> playOffMatches(TournamentMemState tournament, GroupInfo iGru,
            Optional<MatchTag> tag) {
        return playOffService
                .findBaseMatches(tournament, iGru.getCid(), tag)
                .stream()
                .sorted(Comparator.comparing(MatchInfo::getMid))
                .collect(toList());
    }

    private List<Bid> selectQuitingUids(Gid gid, TournamentMemState tournament,
            List<Bid> orderUids, int quits) {
        final List<Bid> quitUids = orderUids.stream()
                .map(tournament::getBidOrExpl)
                .filter(b -> groupService.notExpelledInGroup(tournament, b))
                .limit(quits)
                .map(ParticipantMemState::getBid)
                .collect(toList());
        if (quitUids.size() < quits) {
            Stream.generate(() -> FILLER_LOSER_BID)
                    .limit(quits - quitUids.size())
                    .forEach(quitUids::add);
        }
        log.info("{} quit group {}", quitUids, gid);
        return quitUids;
    }

    private void distributeGroupQuittersBetweenPlayOffMatches(
            TournamentMemState tournament,
            DbUpdater batch, int quits, List<Bid> quitBids,
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
                    quitBids.get(iQuitter), batch);
        }
    }

    private static final Set<BidState> quitOrExpl = ImmutableSet.of(Quit, Expl);

    public static boolean isPyrrhic(ParticipantMemState bid) {
        return bid.getUid().equals(FILLER_LOSER_BID)
                || quitOrExpl.contains(bid.state());
    }

    private void nextMatch(MatchInfo mInfo, TournamentMemState tournament, DbUpdater batch, Bid bid) {
        if (mInfo.hasParticipant(bid)) {
            bidRewalksLadder(tournament, mInfo, batch, bid);
        } else {
            throw internalError("no participant "
                    + bid + " in mid " + mInfo.getMid());
        }
    }

    private void nextMatch(TournamentMemState tournament, Optional<Mid> nMid,
            MatchInfo mInfo, DbUpdater batch, Bid bid,
            TriFunc<PlayOffRule, MatchInfo, ParticipantMemState, BidState> stateF) {
        final ParticipantMemState participant = tournament.getBidOrQuit(bid);
        nMid.ifPresent(wMid -> nextMatch(tournament.getMatchById(wMid), tournament, batch, bid));
        if (!nMid.isPresent()) {
            bidService.setBidState(participant,
                    stateF.apply(tournament.getRule().getPlayOff().get(), mInfo, participant),
                    singleton(participant.getBidState()), batch);
            tournamentTerminator.endOfTournamentCategory(tournament, participant.getCid(), batch);
        }
    }

    private void bidRewalksLadder(TournamentMemState tournament,
            MatchInfo mInfo, DbUpdater batch, Bid bid) {
        final ParticipantMemState participant = tournament.getBidOrQuit(bid);
        if (mInfo.getWinnerId().isPresent()) {
            if (mInfo.getWinnerId().equals(Optional.of(bid))) {
                nextMatch(tournament, mInfo.getWinnerMid(), mInfo,
                        batch, bid, this::playOffMatchWinnerState);
            } else {
                nextMatch(tournament, mInfo.getLoserMid(), mInfo,
                        batch, bid, this::playOffMatchLoserState);
            }
        } else {
            if (isPyrrhic(participant)) {
                mInfo.getLoserMid().ifPresent(
                        lMid -> nextMatch(tournament.getMatchById(lMid), tournament, batch, bid));
            } else {
                bidService.setBidState(participant, Wait, singletonList(participant.getBidState()), batch);
            }
        }
    }

    public void assignBidToMatch(TournamentMemState tournament, Mid mid, Bid bid, DbUpdater batch) {
        log.info("Assign uid {} to mid {} in tid {}", bid, mid, tournament.getTid());
        final MatchInfo mInfo = tournament.getMatchById(mid);
        if (mInfo.addParticipant(bid)) {
            log.info("Match is already complete and doesn't requires to be rescored");
            bidRewalksLadder(tournament, mInfo, batch, bid);
            return;
        }
        matchDao.setParticipant(mInfo, bid, batch);
        final int numberOfParticipants = mInfo.numberOfParticipants();
        if (numberOfParticipants == 2) {
            switch (mInfo.getState()) {
                case Draft:
                    final ParticipantMemState participant = tournament.getBidOrQuit(bid);
                    if (isPyrrhic(participant)) {
                        walkOver(tournament, bid, mInfo, batch);
                    } else if (participant.state() == Wait) {
                        changeStatus(batch, mInfo, Place);
                    } else {
                        throw internalError("unexpected bid "
                                + bid + " state " + participant.state());
                    }
                    break;
                case Auto:
                    autoWinComplete(tournament, mInfo, bid, batch);
                    break;
                default:
                    throw internalError("Unexpected state "
                            + mInfo.getState() + " in " + mid);
            }
        } else if (numberOfParticipants == 1) {
            final ParticipantMemState participant = tournament.getBidOrQuit(bid);
            if (isPyrrhic(participant)) {
                walkOver(tournament, bid, mInfo, batch);
            }
        } else {
            throw internalError("invalid number of participants " + numberOfParticipants);
        }
    }

    private void autoWinComplete(TournamentMemState tournament, MatchInfo mInfo,
            Bid winBid, DbUpdater batch) {
        log.info("Auto complete mid {} due {} quit and {} won",
                mInfo.getMid(), mInfo.getOpponentBid(winBid), winBid);
        completeMatch(mInfo, winBid, batch, singleton(Auto));
        switch (mInfo.getType()) {
            case Brnz:
                checkSinkMatch(mInfo);
                bidService.setBidState(tournament.getBidOrQuit(winBid), Win3, asList(Play, Wait, Rest), batch);
                tournamentTerminator.endOfTournamentCategory(tournament, mInfo.getCid(), batch);
                break;
            case Gold:
                checkSinkMatch(mInfo);
                bidService.setBidState(tournament.getBidOrQuit(winBid), Win1, asList(Play, Wait, Rest), batch);
                tournamentTerminator.endOfTournamentCategory(tournament, mInfo.getCid(), batch);
                break;
            case POff:
                mInfo.getWinnerMid()
                        .ifPresent(wMid -> assignBidToMatch(tournament, wMid, winBid, batch));
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
        matchDao.changeStatus(mInfo, batch);
    }

    private void checkPermissions(TournamentMemState tournament,
            Uid senderUid, Optional<Bid> senderBid,
            MatchInfo matchInfo, SetScoreReq score) {
        final Set<Bid> scoreUids = score.getScores().stream()
                .map(IdentifiedScore::getBid).collect(toSet());
        final Set<Bid> participantIds = matchInfo.getParticipantIdScore().keySet();
        if (!participantIds.equals(scoreUids)) {
            throw badRequest("Some of participants don't play match "
                    + matchInfo.getMid());
        }
        if (!senderBid.map(participantIds::contains).orElse(false)
                && !tournament.isAdminOf(senderUid)) {
            throw forbidden("User " + senderUid
                    + " neither admin of the tournament"
                    + " nor a participant of the match");
        }
        final Map<Bid, Integer> newSetScore = score.getScores().stream()
                .collect(toMap(IdentifiedScore::getBid, IdentifiedScore::getScore));
        final int playedSets = matchInfo.playedSets();
        if (playedSets < score.getSetOrdNumber()) {
            throw badRequest("Set " + playedSets + " needs to be scored first");
        }
        if (tournament.selectMatchRule(matchInfo).countOnlySets()) {
            if (playedSets > 0)  {
                throw badRequest("match with countOnlySets has scored sets");
            }
        }
        if (playedSets > score.getSetOrdNumber()) {
            final Map<Bid, Integer> setScore = matchInfo.getSetScore(score.getSetOrdNumber());
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
                .winBid(matchInfo.getWinnerId())
                .sets(matchInfo.getParticipantIdScore())
                .wonSets(wonSets(tournament, matchInfo))
                .build();
    }

    private Map<Bid, Integer> wonSets(TournamentMemState tournament, MatchInfo matchInfo) {
        if (matchInfo.getParticipantIdScore().size() < 2) {
            return matchInfo.getParticipantIdScore().keySet()
                    .stream()
                    .collect(toMap(o -> o, o -> 0));
        }
        return sports.calcWonSets(tournament, matchInfo);
    }

    public List<MatchInfo> bidIncompleteGroupMatches(Bid bid, TournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(mInfo -> mInfo.getParticipantIdScore().containsKey(bid))
                .filter(mInfo -> mInfo.getGid().isPresent())
                .filter(mInfo -> INCOMPLETE_MATCH_STATES.contains(mInfo.getState()))
                .collect(toList());
    }

    public void leaveFromPlayOff(ParticipantMemState bid, TournamentMemState tournament, DbUpdater batch) {
        playOffMatchForResign(bid.getBid(), tournament)
                .ifPresent(match -> {
                    walkOver(tournament, bid.getBid(), match, batch);
                    leaveFromPlayOff(bid, tournament, batch);
                });
    }

    public void walkOver(TournamentMemState tournament, Bid walkoverBid, MatchInfo mInfo, DbUpdater batch) {
        log.info("Uid {} walkovers mid {}", walkoverBid, mInfo.getMid());
        Optional<Bid> winBid = mInfo.getOpponentBid(walkoverBid);
        if (winBid.isPresent()) {
            matchWinnerDetermined(tournament, mInfo, winBid.get(), batch, EXPECTED_MATCH_STATES);
        } else {
            changeStatus(batch, mInfo, Auto);
            if (mInfo.getType() == Gold) {
                ParticipantMemState bid = tournament.getBidOrQuit(walkoverBid);
                //if (bid.getBidState() == Quit) {
                    bidService.setBidState(bid, Win2, singleton(bid.getBidState()), batch);
                //}
            }
            mInfo.getLoserMid()
                    .ifPresent(lmid -> assignBidToMatch(tournament, lmid, walkoverBid, batch));
        }
    }

    public Optional<MatchInfo> playOffMatchForResign(Bid bid, TournamentMemState tournament) {
        return tournament.getMatches().values()
                .stream()
                .filter(minfo -> minfo.hasParticipant(bid))
                .filter(minfo -> !minfo.getGid().isPresent())
                .filter(minfo -> INCOMPLETE_MATCH_STATES.contains(minfo.getState()))
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
                                        m.bids().stream()
                                                .map(tournament::getParticipant)
                                                .map(ParticipantMemState::toBidLink)
                                                .collect(toList()))
                                .build())
                        .collect(toList()));
    }

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> relatedTidCache;

    @Inject
    private TournamentCache tournamentCache;

    public Collection<Bid> findAllBidsOfUid(TournamentMemState tournament, Uid uid) {
        return ofNullable(tournament.getUidCid2Bid().get(uid))
                .map(Map::values)
                .orElse(Collections.emptyList());
    }

    @SneakyThrows
    public MyPendingMatchList findPendingMatchesIncludeConsole(
            TournamentMemState tournament, Bid bid) {
        return concat(
                Stream.of(findPendingMatches(tournament, bid)),
                childTourProvider.getChildren(tournament)
                        .map((t) -> findPendingMatches(t, bid)))
                .reduce(MyPendingMatchList::merge)
                .orElseThrow(() -> internalError("Tournament " + tournament.getTid() + " disappeared"));
    }

    public MyPendingMatchList findPendingMatches(
            TournamentMemState tournament, Bid bid) {
        return scheduleService.withPlaceTables(tournament, tablesDiscovery ->
                MyPendingMatchList.builder()
                        .matches(tournament.participantMatches(bid)
                                .filter(m -> INCOMPLETE_MATCH_STATES.contains(m.getState()))
                                .sorted(tournament.getRule().getPlace()
                                        .map(PlaceRules::getArenaDistribution).orElse(NO) == NO
                                        ? noTablesParticipantMatchComparator(tournament, bid)
                                        : PARTICIPANT_MATCH_COMPARATOR)
                                .map(m -> MyPendingMatch.builder()
                                        .mid(m.getMid())
                                        .bid(bid)
                                        .table(tablesDiscovery.discover(m.getMid()).map(TableInfo::toLink))
                                        .state(m.getState())
                                        .playedSets(m.playedSets())
                                        .tid(tournament.getTid())
                                        .matchType(m.getType())
                                        .sport(tournament.selectMatchRule(m).toMyPendingMatchSport())
                                        .enemy(m.getOpponentBid(bid).map(oBid ->
                                                ofNullable(tournament.getBid(oBid))
                                                        .orElseThrow(() -> internalError("no opponent for "
                                                                + bid + " in " + m)))
                                                .map(ParticipantMemState::toBidLink))
                                        .build())
                                .collect(toList()))
                        .showTables(tournament.getRule().getPlace().map(PlaceRules::getArenaDistribution)
                                .orElse(NO) != NO)
                        .progress(tournamentProgress(tournament, bid))
                        .bidState(ImmutableMap.of(
                                tournament.getParticipant(bid).getCid(),
                                tournament.getParticipant(bid).state()))
                        .build());
    }

    private TournamentProgress tournamentProgress(TournamentMemState tournament, Bid bid) {
        return TournamentProgress.builder()
                .leftMatches(tournament.participantMatches(bid)
                        .map(MatchInfo::getState)
                        .filter(INCOMPLETE_MATCH_STATES::contains)
                        .count())
                .totalMatches(tournament.participantMatches(bid).count())
                .build();
    }

    private TournamentProgress tournamentProgress(TournamentMemState tournament) {
        return TournamentProgress.builder()
                .leftMatches(tournament.getMatches().values().stream()
                        .map(MatchInfo::getState)
                        .filter(INCOMPLETE_MATCH_STATES::contains)
                        .count())
                .totalMatches(tournament.getMatches().size())
                .build();
    }

    public List<OpenMatchForJudge> findOpenMatchesFurJudge(TournamentMemState tournament) {
        return scheduleService.withPlaceTables(tournament,
                (tablesDiscovery -> tournament.getMatches()
                        .values().stream()
                        .filter(m -> m.getState() == Game)
                        .map(m -> getMatchForJudge(tournament, m, tablesDiscovery))
                        .collect(toList())));
    }

    @Inject
    private ChildTournamentProvider childTourProvider;

    @SneakyThrows
    public OpenMatchForJudgeList findOpenMatchesFurJudgeIncludingConsole(
            TournamentMemState tournament) {
        return concat(
                Stream.of(findOpenMatchesForJudgeList(tournament)),
                childTourProvider.getChildren(tournament)
                        .map(this::findOpenMatchesForJudgeList))
                .reduce(OpenMatchForJudgeList::merge)
                .orElseThrow(
                        () -> internalError("Tournament " + tournament.getTid() + " disappeared"));
    }

    public OpenMatchForJudgeList findOpenMatchesForJudgeList(TournamentMemState tournament) {
        return OpenMatchForJudgeList.builder()
                .matches(findOpenMatchesFurJudge(tournament))
                .progress(tournamentProgress(tournament))
                .build();
    }

    public PlayedMatchList findPlayedMatchesByBid(TournamentMemState tournament, Bid bid) {
        final List<MatchInfo> completeMatches = tournament.participantMatches(bid)
                .filter(m -> m.getState() == Over || m.playedSets() > 0)
                .sorted(Comparator.comparing(m -> m.getEndedAt().orElse(Instant.MIN)))
                .collect(toList());
        return PlayedMatchList.builder()
                .participant(tournament.getParticipant(bid).toBidLink())
                .progress(tournamentProgress(tournament, bid))
                .inGroup(completeMatches.stream().filter(m -> m.getType() == Grup)
                        .map(m -> PlayedMatchLink.builder()
                                .mid(m.getMid())
                                .opponent(m.getOpponentBid(bid)
                                        .map(tournament::getBidOrExpl)
                                        .map(ParticipantMemState::toBidLink)
                                        .get())
                                .winnerBid(m.getWinnerId())
                                .build())
                        .collect(toList()))
                .playOff(completeMatches.stream().filter(m -> m.getType() != Grup)
                        .map(m -> PlayedMatchLink.builder()
                                .mid(m.getMid())
                                .opponent(m.getOpponentBid(bid)
                                        .map(tournament::getBidOrExpl)
                                        .map(ParticipantMemState::toBidLink)
                                        .get())
                                .winnerBid(m.getWinnerId())
                                .build())
                        .collect(toList()))
                .build();
    }

    public MatchResult matchResult(TournamentMemState tournament, Mid mid, Optional<Uid> ouid) {
        final MatchInfo m = tournament.getMatchById(mid);
        return MatchResult.builder()
                .participants(m.getParticipantIdScore().keySet().stream()
                        .map(uid -> tournament.getBidOrExpl(uid).toBidLink())
                        .collect(toList()))
                .score(matchScore(tournament, m))
                .role(detectRole(tournament, m, ouid))
                .state(m.getState())
                .type(m.getType())
                .group(m.getGid().map(gid -> tournament.getGroups().get(gid).toLink()))
                .category(tournament.getCategory(m.getCid()))
                .tid(tournament.getTid())
                .playedSets(m.playedSets())
                .sport(tournament.selectMatchRule(m).toMyPendingMatchSport())
                .build();
    }

    public UserRole detectRole(TournamentMemState tournament, MatchInfo match, Optional<Uid> ouid) {
        return ouid.map(uid -> {
            if (tournament.isAdminOf(uid)) {
                return Admin;
            } else if (resolveUidAndMidToBid(tournament, uid, match)
                    .map(bid -> match.getParticipantIdScore().containsKey(bid)).orElse(false)) {
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
                .sport(tournament.selectMatchRule(m).toMyPendingMatchSport())
                .playedSets(m.playedSets())
                .started(m.getStartedAt())
                .matchType(m.getType())
                .table(tablesDiscovery.discover(m.getMid()).map(TableInfo::toLink))
                .participants(m.getParticipantIdScore().keySet().stream()
                        .map(bid -> tournament.getBidOrExpl(bid).toBidLink())
                        .collect(toList()))
                .build();
    }

    public List<Mid> findMatchesByParticipants(TournamentMemState tournament,
            Bid bid1, Bid bid2) {
        return tournament.getMatches().values().stream()
                .filter(m -> m.hasParticipant(bid1) && m.hasParticipant(bid2))
                .map(MatchInfo::getMid)
                .collect(toList());
    }
}
