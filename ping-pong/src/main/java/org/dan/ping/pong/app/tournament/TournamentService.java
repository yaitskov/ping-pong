package org.dan.ping.pong.app.tournament;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.TERMINAL_STATE;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.MasterOutcome;
import static org.dan.ping.pong.app.group.ConsoleTournament.INDEPENDENT_RULES;
import static org.dan.ping.pong.app.place.PlaceMemState.PID;
import static org.dan.ping.pong.app.sched.ScheduleCtx.SCHEDULE_SELECTOR;
import static org.dan.ping.pong.app.table.TableService.STATE;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentState.Announce;
import static org.dan.ping.pong.app.tournament.TournamentState.Canceled;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentState.Hidden;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.app.tournament.TournamentType.Classic;
import static org.dan.ping.pong.app.tournament.TournamentType.Console;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.CastingLotsService;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.category.CategoryService;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.place.PlaceDao;
import org.dan.ping.pong.app.place.PlaceMemState;
import org.dan.ping.pong.app.place.PlaceService;
import org.dan.ping.pong.app.playoff.PlayOffMatches;
import org.dan.ping.pong.app.playoff.PlayOffResultEntries;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.sched.ScheduleService;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.app.user.UserLinkIf;
import org.dan.ping.pong.app.user.UserRegRequest;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.sys.error.PiPoEx;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class TournamentService {
    private static final ImmutableSet<TournamentState> EDITABLE_STATES = ImmutableSet.of(Hidden, Announce, Draft);
    private static final ImmutableSet<TournamentState> CONFIGURABLE_STATES = EDITABLE_STATES;
    private static final int DAYS_TO_SHOW_COMPLETE_BIDS = 30;
    public static final String UNKNOWN_PLACE = "unknown place";
    public static final String TID = "tid";
    public static final String PLACE_IS_BUSY = "place-is-busy";
    private static final List<BidState> VALID_ENLIST_BID_STATES = asList(Want, Paid, Here);

    @Inject
    private TournamentDaoMySql tournamentDao;

    @Inject
    private PlaceDao placeDao;

    @Transactional(TRANSACTION_MANAGER)
    public Tid create(Uid uid, CreateTournament newTournament) {
        final PlaceMemState place = placeDao.load(newTournament.getPlaceId())
                .orElseThrow(() -> badRequest(UNKNOWN_PLACE, PID, newTournament.getPlaceId()));
        place.checkAdmin(uid);
        return tournamentDao.create(uid, newTournament);
    }

    private void validateEnlistOnline(TournamentMemState tournament, Enlist enlist) {
        if (tournament.getState() != Draft) {
            throw notDraftError(tournament);
        }
        validateEnlist(tournament, enlist);
    }

    private void validateEnlistOffline(TournamentMemState tournament, EnlistOffline enlist) {
        if (tournament.getState() == Draft) {
            if (!VALID_ENLIST_BID_STATES.contains(enlist.getBidState())) {
                throw badRequest("Bid state could be " + VALID_ENLIST_BID_STATES);
            }
            enlist.getGroupId()
                    .ifPresent(gid -> { throw badRequest("group is not expected"); });
        } else if (tournament.getState() == Open) {
            if (enlist.getBidState() != Wait) {
                throw badRequest("Bid state should be Wait");
            }
            final Optional<Integer> ogid = enlist.getGroupId();
            if (ogid.isPresent()) {
                if (!groupService.isNotCompleteGroup(tournament, ogid.get())) {
                    throw badRequest("group is complete");
                }
            } else {
                groupService.ensureThatNewGroupCouldBeAdded(tournament, enlist.getCid());
            }
        } else {
            throw notDraftError(tournament);
        }
        validateEnlist(tournament, enlist);
    }

    private PiPoEx notDraftError(TournamentMemState tournament) {
        return badRequest("tournament-not-in-draft",
                ImmutableMap.of(STATE, tournament.getState(),
                        TID, tournament.getTid()));
    }

    private void validateEnlist(TournamentMemState tournament, Enlist enlist) {
        if (tournament.getType() != Classic) {
            throw badRequest("Tournament does not allow direct enlistment");
        }
        tournament.checkCategory(enlist.getCid());
        final CastingLotsRule casting = tournament.getRule().getCasting();
        if (casting.getPolicy() == ParticipantRankingPolicy.ProvidedRating) {
            final int rank = enlist.getProvidedRank()
                    .orElseThrow(() -> badRequest("Ranking is required in",
                            TID, tournament.getTid()));
            casting.getProvidedRankOptions()
                    .orElseThrow(() -> internalError("no rank options"))
                    .validate(rank);
        } else {
            if (enlist.getProvidedRank().isPresent()) {
                throw badRequest("Provided ranking is not used",
                        TID, tournament.getTid());
            }
        }
    }

    public void enlistOnline(EnlistTournament enlistment,
            TournamentMemState tournament, UserLinkIf user, DbUpdater batch) {
        validateEnlistOnline(tournament, enlistment);
        enlistOnlineWithoutValidation(enlistment, tournament, user, batch);
    }

    public void enlistOnlineWithoutValidation(EnlistTournament enlistment, TournamentMemState tournament, UserLinkIf user, DbUpdater batch) {
        log.info("Uid {} enlists to tid {} in cid {}",
                user.getUid(), tournament.getTid(), enlistment.getCategoryId());
        final Uid uid = user.getUid();
        final Instant now = clocker.get();
        tournament.getParticipants().put(uid, ParticipantMemState.builder()
                .bidState(enlistment.getBidState())
                .uid(uid)
                .name(user.getName())
                .updatedAt(now)
                .enlistedAt(now)
                .cid(enlistment.getCategoryId())
                .tid(tournament.getTid())
                .build());
        enlist(tournament, uid, enlistment.getProvidedRank(), batch, Optional.empty());
    }

    private void enlist(TournamentMemState tournament, Uid uid,
            Optional<Integer> providedRank, DbUpdater batch, Optional<Integer> oGid) {
        bidDao.enlist(tournament.getParticipant(uid), providedRank, batch);
        if (tournament.getState() == Open) {
            oGid.ifPresent(gid ->
                    castingLotsService.addParticipant(uid, tournament, batch));
        }
    }

    public List<TournamentDigest> findInWithEnlisted(Uid uid, int days) {
        return tournamentDao.findEnlistedIn(uid,
                clocker.get().minus(days, DAYS));
    }

    public List<DatedTournamentDigest> findDrafting() {
        return tournamentDao.findByState(TournamentState.Draft);
    }

    @Inject
    private BidDao bidDao;

    @Inject
    private CastingLotsService castingLotsService;

    @Inject
    private Clocker clocker;

    @Inject
    @Named(SCHEDULE_SELECTOR)
    private ScheduleService scheduleService;

    public void begin(TournamentMemState tournament, DbUpdater batch) {
        if (tournament.getState() != TournamentState.Draft) {
            throw notDraftError(tournament);
        }
        final List<ParticipantMemState> readyBids = castingLotsService.findBidsReadyToPlay(tournament);
        castingLotsService.checkAllThatAllHere(readyBids);
        if (readyBids.isEmpty()) {
            tournament.setState(TournamentState.Close);
            tournamentDao.setState(tournament, batch);
            return;
        } else if (readyBids.size() == 1) {
            bidService.setBidState(readyBids.get(0), BidState.Win1,
                    singletonList(readyBids.get(0).getBidState()), batch);
            tournament.setState(TournamentState.Close);
            tournamentDao.setState(tournament, batch);
            return;
        }
        readyBids.forEach(bid ->
                bidService.setBidState(bid, BidState.Wait,
                        singletonList(bid.getBidState()), batch));
        castingLotsService.seed(tournament, readyBids, batch);
        tournament.setState(TournamentState.Open);
        tournamentDao.setState(tournament, batch);
    }

    public void beginAndSchedule(TournamentMemState tournament, DbUpdater batch) {
        begin(tournament, batch);
        scheduleService.beginTournament(tournament, batch, clocker.get());
    }

    @Inject
    private CategoryDao categoryDao;

    @Inject
    private SequentialExecutor sequentialExecutor;

    @Inject
    private PlaceService placeCache;

    public DraftingTournamentInfo getDraftingTournament(TournamentMemState tournament,
            Optional<Uid> participantId) {
        return sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> DraftingTournamentInfo.builder()
                        .tid(tournament.getTid())
                        .rules(tournament.getRule())
                        .name(tournament.getName())
                        .state(tournament.getState())
                        .enlisted(tournament.getParticipants().size())
                        .ticketPrice(tournament.getTicketPrice())
                        .previousTid(tournament.getPreviousTid())
                        .bidState(participantId.map(tournament::getBid)
                                .filter(Objects::nonNull)
                                .map(ParticipantMemState::getBidState))
                        .place(place.toLink())
                        .categories(tournament.getCategories().values())
                        .opensAt(tournament.getOpensAt())
                        .myCategoryId(participantId.map(tournament::getBid)
                                .filter(Objects::nonNull)
                                .map(ParticipantMemState::getCid))
                        .iAmAdmin(participantId.filter(tournament::isAdminOf).isPresent())
                        .rules(tournament.getRule())
                        .build());
    }

    @SneakyThrows
    public MyTournamentInfo getMyTournamentInfo(TournamentMemState tournament) {
        final RelatedTids relatedTids = tournamentRelations.get(tournament.getTid());
        return sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> MyTournamentInfo.builder()
                        .categories(tournament.getCategories().size())
                        .opensAt(tournament.getOpensAt())
                        .previousTid(tournament.getPreviousTid())
                        .price(tournament.getTicketPrice())
                        .place(place.toLink())
                        .state(tournament.getState())
                        .enlisted(tournament.getParticipants().size())
                        .name(tournament.getName())
                        .tid(tournament.getTid())
                        .consoleTid(relatedTids.getChild())
                        .masterTid(relatedTids.getParent())
                        .build());
    }

    public void leaveTournament(ParticipantMemState bid, TournamentMemState tournament,
            BidState targetState, DbUpdater batch) {
        final Tid tid = tournament.getTid();
        log.info("User {} leaves tournament {} as {}", bid.getUid(), tid, targetState);
        final TournamentState state = tournament.getState();
        switch (state) {
            case Close:
            case Canceled:
            case Replaced:
                throw badRequest("Tournament is complete");
            case Open:
                leaveOpenTournament(bid, tournament, targetState, batch);
                break;
            case Hidden:
            case Announce:
            case Draft:
                bidService.setBidState(bid, targetState, singletonList(bid.getBidState()), batch);
                break;
            default:
                throw internalError("State " + state + " is not supported");
        }
    }

    private void leaveOpenTournament(ParticipantMemState bid, TournamentMemState tournament,
            BidState targetState, DbUpdater batch) {
        final Tid tid = tournament.getTid();
        log.info("User {} leaves open tournament {}", bid.getUid(), tid);
        switch (bid.getBidState()) {
            case Play:
            case Rest:
            case Wait:
                activeParticipantLeave(bid, tournament, clocker.get(), targetState, batch);
                break;
            case Win1:
            case Win2:
            case Win3:
            case Quit:
            case Expl:
                throw badRequest("Participant is in a terminal state");
            case Want:
            case Paid:
            case Here:
                bidService.setBidState(bid, targetState, singletonList(bid.getBidState()), batch);
                break;
            default:
                throw internalError("Unknown state " + bid.getBidState());
        }
    }

    @Inject
    private MatchService matchService;

    @Inject
    private BidService bidService;

    @Value("${match.score.timeout}")
    private int matchScoreTimeout;

    public void activeParticipantLeave(ParticipantMemState bid, TournamentMemState tournament,
            Instant now, BidState target, DbUpdater batch) {
        final Uid uid = bid.getUid();
        List<MatchInfo> incompleteMy = matchService.bidIncompleteGroupMatches(uid, tournament);
        log.info("activeParticipantLeave uid {} incomplete {}", uid, incompleteMy.size());
        if (incompleteMy.isEmpty()) {
            matchService.leaveFromPlayOff(bid, tournament, batch);
        } else {
            bidService.setBidState(bid, target, singletonList(bid.getBidState()), batch);
            for (MatchInfo match : incompleteMy) {
                matchService.walkOver(tournament, uid, match, batch);
            }
        }
        if (bid.getBidState() != Win2 || target == Expl) {
            bidService.setBidState(bid, target, singletonList(bid.getBidState()), batch);
        }
        scheduleService.participantLeave(tournament, batch, now);
    }

    public List<OpenTournamentDigest> findRunning(int completeInLastDays) {
        return tournamentDao.findRunning(
                clocker.get().minus(completeInLastDays, DAYS));
    }

    public MyRecentTournaments findMyRecentTournaments(Uid uid) {
        return tournamentDao.findMyRecentTournaments(
                clocker.get().minus(DAYS_TO_SHOW_COMPLETE_BIDS, DAYS),
                uid);
    }

    public MyRecentJudgedTournaments findMyRecentJudgedTournaments(Uid uid) {
        return tournamentDao.findMyRecentJudgedTournaments(clocker.get(), uid);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void update(TournamentMemState tournament, TournamentUpdate update, DbUpdater batch) {
        if (!EDITABLE_STATES.contains(tournament.getState())) {
            throw badRequest("Tournament could be modified until it's open");
        }
        tournament.setPid(update.getPlaceId());
        tournament.setTicketPrice(update.getPrice());
        tournament.setName(update.getName());
        tournament.setOpensAt(update.getOpensAt());
        tournamentDao.update(update, batch);
    }

    public void updateTournamentParams(TournamentMemState tournament,
            TidIdentifiedRules parameters, DbUpdater batch) {
        if (!CONFIGURABLE_STATES.contains(tournament.getState())) {
            throw badRequest("Tournament cannot be modified in state",
                    ImmutableMap.of(TID, tournament.getTid(),
                            "state", tournament.getState()));
        }
        if (tournament.getSport() != parameters.getRules().getMatch().sport()) {
            throw badRequest("sport mismatch");
        }
        if (tournament.getType() == Console && parameters.getRules().getGroup().isPresent()) {
            throw badRequest("console tournament cannot have groups");
        }
        tournament.setRule(parameters.getRules());
        tournamentDao.updateParams(tournament.getTid(), tournament.getRule(), batch);
    }

    @Inject
    private MatchDao matchDao;

    @Inject
    private GroupDao groupDao;

    public void cancel(TournamentMemState tournament, DbUpdater batch) {
        final Instant now = clocker.get();

        final Tid tid = tournament.getTid();
        tournamentTerminator.setTournamentState(tournament, Canceled, batch);
        tournamentTerminator.setTournamentCompleteAt(tournament, clocker.get(), batch);
        scheduleService.cancelTournament(tournament, batch, now);
        matchDao.deleteAllByTid(tournament, batch, tournament.getMatches().size());
        tournament.getParticipants().values().stream()
                .filter(bid -> bid.getState() != Quit)
                .forEach(bid -> bid.setBidState(Want));
        bidDao.resetStateByTid(tid, now, batch);
        groupDao.deleteAllByTid(tournament.getTid(), batch, tournament.getGroups().size());
        tournament.getMatches().clear();
        tournament.getGroups().clear();
    }

    @Inject
    private PlayOffService playOffService;

    @Inject
    private GroupService groupService;

    public List<TournamentResultEntry> tournamentResult(TournamentMemState tournament, int cid) {
        final List<TournamentResultEntry> groupOrdered = tournament.getRule().getGroup()
                .map(gr -> groupService.resultOfAllGroupsInCategory(tournament, cid))
                .orElse(emptyList());
        final PlayOffResultEntries playOffResult = playOffService
                .playOffResult(tournament, cid, groupOrdered);

        if (playOffResult.getEntries().isEmpty()) {
            return groupOrdered;
        } else {
            combineGroupAndPlayOffEntries(groupOrdered, playOffResult);
            return playOffResult.getEntries();
        }
    }

    private void combineGroupAndPlayOffEntries(
            List<TournamentResultEntry> groupOrdered,
            PlayOffResultEntries playOffResult) {
        if (groupOrdered.isEmpty()) {
            return;
        }
        final Map<Uid, List<Optional<Reason>>> reasonChains = new HashMap<>();
        final List<TournamentResultEntry> leftInGroup = groupOrdered.stream()
                .filter(e -> {
                    if (playOffResult.getPlayOffUids()
                            .contains(e.getUser().getUid())) {
                        reasonChains.put(e.getUser().getUid(), e.getReasonChain());
                        return false;
                    }
                    return true;
                })
                .collect(toList());

        playOffResult.getEntries().forEach(
                e -> e.getReasonChain().addAll(reasonChains.get(e.getUser().getUid())));
        playOffResult.getEntries().addAll(leftInGroup);
    }

    public TournamentComplete completeInfo(TournamentMemState tournament) {
        return TournamentComplete
                .builder()
                .state(tournament.getState())
                .name(tournament.getName())
                .categories(tournament.getCategories().values())
                .hasGroups(tournament.getRule().getGroup().isPresent())
                .hasPlayOff(tournament.getRule().getPlayOff().isPresent())
                .build();
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<TournamentDigest> findWritableForAdmin(Uid uid, int days) {
        return tournamentDao.findWritableForAdmin(uid,
                clocker.get().minus(days, DAYS));
    }

    @Inject
    private UserDao userDao;

    public Uid enlistOffline(TournamentMemState tournament,
            EnlistOffline enlistment, DbUpdater batch) {
        validateEnlistOffline(tournament, enlistment);
        final Uid participantUid = userDao.register(UserRegRequest.builder()
                .name(enlistment.getName())
                .build());
        final Instant now = clocker.get();
        if (tournament.getState() == Open && !enlistment.getGroupId().isPresent()) {
            enlistment.setGroupId(Optional.of(
                    castingLotsService.addGroup(tournament, batch, enlistment.getCid())));
        }
        tournament.getParticipants().put(participantUid, ParticipantMemState.builder()
                .bidState(enlistment.getBidState())
                .enlistedAt(now)
                .updatedAt(now)
                .name(enlistment.getName())
                .cid(enlistment.getCid())
                .uid(participantUid)
                .gid(enlistment.getGroupId())
                .tid(tournament.getTid())
                .build());
        enlist(tournament, participantUid, enlistment.getProvidedRank(),
                batch, enlistment.getGroupId());

        return participantUid;
    }

    @SneakyThrows
    @Transactional(TRANSACTION_MANAGER)
    public Tid copy(CopyTournament copyTournament) {
        final Tid masterCopyTid = tournamentDao.copy(copyTournament);
        categoryDao.copy(copyTournament.getOriginTid(), masterCopyTid);
        final RelatedTids relatedTids = tournamentRelations
                .get(copyTournament.getOriginTid());
        relatedTids.getChild().ifPresent(childTid -> {
            final Tid consoleCopyTid = copy(copyTournament.withOriginTid(childTid));
            tournamentDao.createRelation(masterCopyTid, consoleCopyTid);
        });
        return masterCopyTid;
    }

    @Inject
    private CategoryService categoryService;

    @Inject
    private TournamentTerminator tournamentTerminator;

    public PlayOffMatches playOffMatches(TournamentMemState tournament, int cid) {
        return playOffService.playOffMatches(tournament, cid, Optional.empty());
    }

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> tournamentRelations;

    @Inject
    private TournamentCache tournamentCache;

    @SneakyThrows
    public Tid createConsoleFor(TournamentMemState masterTournament, UserInfo user,
            DbUpdater batch) {
        final RelatedTids relatedTids = tournamentRelations.get(masterTournament.getTid());
        if (relatedTids.getChild().isPresent()) {
            return relatedTids.getChild().get();
        }
        if (masterTournament.getType() != Classic) {
            throw badRequest("Tournament " + masterTournament.getType()
                    + " does not support console tournaments");
        }
        if (!masterTournament.getRule().getGroup().isPresent()) {
            throw badRequest("Tournament " + masterTournament.getTid()
                    + " has no groups");
        }
        final Tid consoleTid = create(user.getUid(),
                CreateTournament.builder()
                        .sport(masterTournament.getSport())
                        .ticketPrice(masterTournament.getTicketPrice())
                        .name(masterTournament.getName())
                        .placeId(masterTournament.getPid())
                        .opensAt(masterTournament.getOpensAt())
                        .previousTid(Optional.of(masterTournament.getTid()))
                        .state(Draft)
                        .type(Console)
                        .rules(TournamentRules.builder()
                                .place(masterTournament.getRule().getPlace())
                                .match(masterTournament.getRule().getMatch())
                                .playOff(masterTournament.getRule().getPlayOff())
                                .casting(masterTournament.getRule()
                                        .getCasting().withPolicy(MasterOutcome))
                                .rewards(Optional.empty())
                                .group(Optional.empty())
                                .build())
                        .build());

        tournamentDao.createRelation(masterTournament.getTid(), consoleTid);

        masterTournament.setRule(masterTournament.getRule()
                .withGroup(masterTournament.getRule().getGroup()
                        .map(g -> g.withConsole(INDEPENDENT_RULES))));
        tournamentDao.updateParams(masterTournament.getTid(), masterTournament.getRule(), batch);

        masterTournament.getRule().getGroup().ifPresent(groupRules ->
                enlistPlayersFromCompleteGroups(masterTournament,
                        tournamentCache.load(consoleTid),
                        batch));
        tournamentRelations.invalidate(masterTournament.getTid());

        return consoleTid;
    }

    private void enlistPlayersFromCompleteGroups(TournamentMemState masterTournament,
            TournamentMemState consoleTournament, DbUpdater batch) {
        final Map<Integer, List<MatchInfo>> matchesByGroup = groupService.groupMatchesByGroup(masterTournament);
        final Set<Integer> incompleteGroups = groupService.findIncompleteGroups(masterTournament);
        incompleteGroups.forEach(matchesByGroup::remove);

        matchesByGroup.forEach((gid, groupMatches) -> {
            final int quitsGroup = masterTournament.getRule().getGroup().get().getQuits();
            final List<Uid> orderedGroupUids = groupService.orderUidsInGroup(gid, masterTournament, groupMatches);
            final int consoleCid = categoryService.findCidOrCreate(masterTournament, gid, consoleTournament, batch);

            orderedGroupUids.stream().skip(quitsGroup)
                    .map(masterTournament::getBidOrQuit)
                    .forEach(bid ->
                            enlistOnlineWithoutValidation(
                                    EnlistTournament.builder()
                                            .categoryId(consoleCid)
                                            .bidState(
                                                    TERMINAL_STATE.contains(bid.getBidState())
                                                            ? bid.getBidState()
                                                            : Here)
                                            .providedRank(Optional.empty())
                                            .build(),
                                    consoleTournament, bid, batch));
        });
    }

    public List<TournamentDigest> findFollowingFrom(Tid tid) {
        return tournamentDao.findFollowingFrom(tid);
    }
}
