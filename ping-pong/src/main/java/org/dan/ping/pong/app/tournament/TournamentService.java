package org.dan.ping.pong.app.tournament;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.ConsoleLayered;
import static org.dan.ping.pong.app.category.CategoryService.groupByCategories;
import static org.dan.ping.pong.app.category.CategoryState.Drt;
import static org.dan.ping.pong.app.category.CategoryState.Ply;
import static org.dan.ping.pong.app.category.SelectedCid.selectCid;
import static org.dan.ping.pong.app.group.ConsoleTournament.NO;
import static org.dan.ping.pong.app.place.PlaceMemState.PID;
import static org.dan.ping.pong.app.table.TableService.STATE;
import static org.dan.ping.pong.app.tournament.EnlistPolicy.ONCE_PER_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentState.Announce;
import static org.dan.ping.pong.app.tournament.TournamentState.Canceled;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentState.Hidden;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.app.tournament.TournamentType.Classic;
import static org.dan.ping.pong.app.tournament.TournamentType.Console;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConGru;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConOff;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.SelectedBid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.CastingLotsService;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.category.CategoryService;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.group.GroupRemover;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchRemover;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.place.PlaceDao;
import org.dan.ping.pong.app.place.PlaceMemState;
import org.dan.ping.pong.app.place.PlaceService;
import org.dan.ping.pong.app.playoff.PlayOffMatches;
import org.dan.ping.pong.app.playoff.PlayOffResultEntries;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.sched.ScheduleServiceSelector;
import org.dan.ping.pong.app.suggestion.SuggestionService;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.app.user.UserLinkIf;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.sys.error.PiPoEx;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class TournamentService {
    private static final ImmutableSet<TournamentState> EDITABLE_STATES = ImmutableSet.of(Hidden, Announce, Draft);
    private static final ImmutableSet<TournamentState> CONFIGURABLE_STATES = EDITABLE_STATES;
    private static final int DAYS_TO_SHOW_COMPLETE_BIDS = 30;
    public static final String UNKNOWN_PLACE = "unknown place";
    private static final List<BidState> VALID_ENLIST_BID_STATES = asList(Want, Paid, Here);
    public static final PlayOffResultEntries EMPTY_PLAYOFF = PlayOffResultEntries.builder().entries(emptyList()).build();

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

    private void validateEnlistOnline(TournamentMemState tournament, Enlist enlist, Uid uid) {
        if (tournament.getState() != Draft) {
            throw notDraftError(tournament);
        }
        validateEnlist(tournament, enlist, uid);
    }

    private void validateEnlistOffline(TournamentMemState tournament, EnlistOffline enlist) {
        if (tournament.getState() == Draft) {
            if (!VALID_ENLIST_BID_STATES.contains(enlist.getBidState())) {
                throw badRequest("Bid state could be "
                        + VALID_ENLIST_BID_STATES + " but " + enlist.getBidState());
            }
            enlist.getGroupId()
                    .ifPresent(gid -> { throw badRequest("group is not expected"); });
        } else if (tournament.getState() == Open) {
            if (enlist.getBidState() != Wait) {
                throw badRequest("Bid state should be Wait");
            }
            final Optional<Gid> ogid = enlist.getGroupId();
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
        validateEnlist(tournament, enlist, enlist.getUid());
    }

    private PiPoEx notDraftError(TournamentMemState tournament) {
        return badRequest("tournament-not-in-draft",
                ImmutableMap.of(STATE, tournament.getState(),
                        TournamentMemState.TID, tournament.getTid()));
    }

    private void validateEnlist(TournamentMemState tournament, Enlist enlist, Uid uid) {
        if (tournament.getType() != Classic) {
            throw badRequest("Tournament does not allow direct enlistment");
        }

        tournament.getRule().getEnlist()
                .orElse(ONCE_PER_TOURNAMENT)
                .validate(tournament, enlist.getCid(), uid);

        tournament.checkCategory(enlist.getCid());
        final CastingLotsRule casting = tournament.getRule().getCasting();
        if (casting.getPolicy() == ParticipantRankingPolicy.ProvidedRating) {
            final int rank = enlist.getProvidedRank()
                    .orElseThrow(() -> badRequest("Ranking is required in",
                            TournamentMemState.TID, tournament.getTid()));
            casting.getProvidedRankOptions()
                    .orElseThrow(() -> internalError("no rank options"))
                    .validate(rank);
        } else {
            if (enlist.getProvidedRank().isPresent()) {
                throw badRequest("Provided ranking is not used",
                        TournamentMemState.TID, tournament.getTid());
            }
        }
    }

    public Bid enlistOnline(EnlistTournament enlistment,
            TournamentMemState tournament, UserLinkIf user, DbUpdater batch) {
        validateEnlistOnline(tournament, enlistment, user.getUid());
        return enlistOnlineWithoutValidation(enlistment, tournament, user, batch);
    }

    public Bid enlistOnlineWithoutValidation(
            EnlistTournament enlistment, TournamentMemState tournament,
            UserLinkIf user, DbUpdater batch) {
        log.info("Uid {} enlists to tid {} in cid {}",
                user.getUid(), tournament.getTid(), enlistment.getCategoryId());
        final Bid bid = tournament.allocateBidFor(enlistment.getCategoryId(), user.getUid());
        return enlistToConsole(bid, enlistment, tournament, user, batch);
    }

    public Bid enlistToConsole(Bid bid, EnlistTournament enlistment,
            TournamentMemState tournament, UserLinkIf user, DbUpdater batch) {
        final Instant now = clocker.get();
        tournament.registerParticipant(
                ParticipantMemState.builder()
                        .bidState(enlistment.getBidState())
                        .bid(bid)
                        .uid(user.getUid())
                        .name(user.getName())
                        .updatedAt(now)
                        .enlistedAt(now)
                        .cid(enlistment.getCategoryId())
                        .tid(tournament.getTid())
                        .build());
        enlist(tournament, bid, enlistment.getProvidedRank(), batch, Optional.empty());
        return bid;
    }

    private void enlist(TournamentMemState tournament, Bid bid,
            Optional<Integer> providedRank, DbUpdater batch, Optional<Gid> oGid) {
        bidDao.enlist(tournament.getParticipant(bid), providedRank, batch);
        if (tournament.getState() == Open) {
            oGid.ifPresent(gid -> {
                if (!tournament.disambiguationMatchNotPossible()) {
                    matchRemover.deleteByMids(
                            tournament, batch, tournament.getMatches()
                                    .values().stream()
                                    .filter(m -> m.getGid().equals(oGid)
                                            && m.getTag().isPresent())
                                    .map(MatchInfo::getMid)
                                    .collect(toSet()));
                }
                castingLotsService.addParticipant(bid, tournament, batch);
            });
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
    private ScheduleServiceSelector scheduleService;

    public void begin(TournamentMemState tournament, DbUpdater batch) {
        if (tournament.getState() != TournamentState.Draft) {
            throw notDraftError(tournament);
        }
        final List<ParticipantMemState> readyBids = castingLotsService
                .findBidsReadyToPlay(tournament);
        castingLotsService.checkAllThatAllHere(readyBids);
        final Map<Cid, List<ParticipantMemState>> cid2Par = groupByCategories(readyBids);
        tournament.getCategories().entrySet().stream()
                .filter(e -> e.getValue().getState() == Drt)
                .map(Map.Entry::getKey)
                .forEach(cid -> beginCategory(
                        selectCid(cid, tournament, batch),
                        ofNullable(cid2Par.get(cid))
                                .orElseGet(Collections::emptyList)));
    }

    @Inject
    private CategoryService categoryService;

    private void beginCategory(SelectedCid sCid, List<ParticipantMemState> bids) {
        log.info("Begin category {} of tournament {}", sCid.cid(), sCid.tid());
        categoryService.setState(sCid.tid(), sCid.category(), Ply, sCid.batch());
        switch (bids.size()) {
            case 1:
                sCid.rules().getGroup()
                        .ifPresent((gRu) ->
                                bidService.setGroupForUids(
                                        sCid.batch(),
                                        groupService.createGroup(sCid),
                                        sCid.tid(),
                                        bids));
                bidService.setBidState(bids.get(0), Win1, sCid.batch());
            case 0:
                return;
            default:
                bids.forEach(bid -> bidService.setBidState(bid, Wait, sCid.batch()));
                castingLotsService.seedCategory(sCid, bids);
                setState(sCid.tournament(), Open, sCid.batch());
        }
    }

    private void setState(
            TournamentMemState tournament, TournamentState targetSt, DbUpdater batch) {
        tournament.setState(targetSt);
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

    public DraftingTournamentInfo getDraftingTournament(
            TournamentMemState tournament, Optional<Uid> userId) {
        return sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> DraftingTournamentInfo.builder()
                        .tid(tournament.getTid())
                        .rules(tournament.getRule())
                        .name(tournament.getName())
                        .state(tournament.getState())
                        .enlisted(tournament.getParticipants().size())
                        .ticketPrice(tournament.getTicketPrice())
                        .previousTid(tournament.getPreviousTid())
                        .place(place.toLink())
                        .categories(tournament.categoryLinks())
                        .opensAt(tournament.getOpensAt())
                        .categoryState(userId
                                .flatMap(u -> ofNullable(tournament.getUidCid2Bid().get(u)))
                                .map(cid2Bid -> cid2Bid
                                        .entrySet()
                                        .stream()
                                        .collect(toMap(
                                                Map.Entry::getKey,
                                                e -> tournament
                                                        .getParticipant(e.getValue())
                                                        .getBidState())))
                                .orElse(emptyMap()))
                        .iAmAdmin(userId.filter(tournament::isAdminOf).isPresent())
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
                        .consoleGroupTid(ofNullable(relatedTids.getChildren().get(ConGru)))
                        .consolePlayOffTid(ofNullable(relatedTids.getChildren().get(ConOff)))
                        .master(relatedTids.getParent())
                        .build());
    }

    public void leaveTournament(SelectedBid sBid, BidState targetState) {
        log.info("User {} leaves tournament {} as {}",
                sBid.uid(), sBid.tid(), targetState);
        switch (sBid.tidState()) {
            case Close:
            case Canceled:
            case Replaced:
                throw badRequest("Tournament is complete");
            case Open:
                leaveOpenTournament(sBid, targetState);
                break;
            case Hidden:
            case Announce:
            case Draft:
                bidService.setBidState(sBid, targetState);
                break;
            default:
                throw internalError("State " + sBid.tidState() + " is not supported");
        }
    }

    private void leaveOpenTournament(SelectedBid sBid, BidState targetState) {
        log.info("User {} leaves open tournament {}", sBid.uid(), sBid.tid());
        switch (sBid.bidState()) {
            case Play:
            case Rest:
            case Wait:
                activeParticipantLeave(sBid, clocker.get(), targetState);
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
                bidService.setBidState(sBid.getBid(), targetState,
                        singletonList(sBid.bidState()), sBid.getBatch());
                break;
            default:
                throw internalError("Unknown state " + sBid.bidState());
        }
    }

    @Inject
    private MatchService matchService;

    @Inject
    private BidService bidService;

    @Value("${match.score.timeout}")
    private int matchScoreTimeout;

    public void activeParticipantLeave(SelectedBid sBid, Instant now, BidState target) {
        final Bid bid = sBid.bid();
        final List<MatchInfo> incompleteMy = matchService
                .bidIncompleteGroupMatches(bid, sBid.getTournament());
        log.info("activeParticipantLeave bid {} incomplete {}", bid, incompleteMy.size());
        if (incompleteMy.isEmpty()) {
            matchService.leaveFromPlayOff(sBid);
        } else {
            bidService.setBidState(sBid, target);
            for (MatchInfo match : incompleteMy) {
                matchService.walkOver(sBid.getTournament(), bid, match, sBid.getBatch());
            }
        }
        if (sBid.bidState() != Win2 || target == Expl) {
            bidService.setBidState(sBid, target);
        }
        scheduleService.participantLeave(sBid.getTournament(), sBid.getBatch(), now);
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

    public void update(
            TournamentMemState tournament, TournamentUpdate update, DbUpdater batch) {
        if (!EDITABLE_STATES.contains(tournament.getState())) {
            throw badRequest("Tournament could be modified until it's open");
        }
        tournament.setPid(update.getPlaceId());
        tournament.setTicketPrice(update.getPrice());
        tournament.setName(update.getName());
        tournament.setOpensAt(update.getOpensAt());
        tournamentDao.update(update, batch);
    }

    private void validateRulesWithTournament(
            TournamentMemState tournament, TournamentRules rules) {
        switch (tournament.getType()) {
            case Classic:
                break; // ok
            case Console:
                validateRulesOfConsoleTournament(tournament, rules);
                break;
            default:
                throw internalError(
                        "Tournament type " + tournament.getType() + " not supported");
        }
    }

    @SneakyThrows
    private void validateRulesOfConsoleTournament(
            TournamentMemState conTour, TournamentRules rules) {
        rules.getGroup().ifPresent(gr -> {
            if (gr.getConsole() != NO) {
                throw badRequest("console group cannot have console tournament");
            }
        });
        rules.getPlayOff().ifPresent(pr -> {
            if (pr.getConsole() != NO) {
                throw badRequest("console playOff cannot have console tournament");
            }
            if (!pr.getLayerPolicy().isPresent()) {
                throw badRequest("tournament for console playOff must have layer policy");
            }
        });
    }

    public void updateTournamentParams(TournamentMemState tournament,
            TidIdentifiedRules parameters, DbUpdater batch) {
        validateRulesWithTournament(tournament, parameters.getRules());
        if (!CONFIGURABLE_STATES.contains(tournament.getState())) {
            throw badRequest("Tournament cannot be modified in state",
                    ImmutableMap.of(TournamentMemState.TID, tournament.getTid(),
                            "state", tournament.getState()));
        }
        final TournamentRules rules = parameters.getRules();
        if (tournament.getSport() != rules.getMatch().sport()) {
            throw badRequest("sport mismatch");
        }
        if (tournament.getType() == Console
                && rules.getCasting().getSplitPolicy() == ConsoleLayered
                && rules.getGroup().isPresent()) {
            throw badRequest("layered console tournament cannot have groups");
        }
        tournament.setRule(rules);
        tournamentDao.updateParams(tournament.getTid(), tournament.getRule(), batch);
    }

    @Inject
    private MatchRemover matchRemover;

    @Inject
    private GroupRemover groupRemover;

    @Inject
    private TournamentTerminator tournamentTerminator;

    public void cancel(TournamentMemState tournament, DbUpdater batch) {
        final Instant now = clocker.get();
        final Tid tid = tournament.getTid();
        tournamentTerminator.setTournamentState(tournament, Canceled, batch);
        tournamentTerminator.setTournamentCompleteAt(tournament, clocker.get(), batch);
        scheduleService.cancelTournament(tournament, batch, now);
        matchRemover.removeByTournament(tournament, batch);
        tournament.getParticipants().values().stream()
                .filter(bid -> bid.state() != Quit)
                .forEach(bid -> bid.setBidState(Want));
        bidDao.resetStateByTid(tid, now, batch);
        groupRemover.removeByTournament(tournament, batch);
    }

    @Inject
    private PlayOffService playOffService;

    @Inject
    private GroupService groupService;

    public List<TournamentResultEntry> tournamentResult(TournamentMemState tournament, Cid cid) {
        final List<TournamentResultEntry> groupOrdered = tournament.getRule().getGroup()
                .map(gr -> groupService.resultOfAllGroupsInCategory(tournament, cid))
                .orElse(emptyList());
        final PlayOffResultEntries playOffResult = tournament.getRule().getPlayOff()
                .map(po -> playOffService.playOffResult(tournament, cid, groupOrdered))
                .orElse(EMPTY_PLAYOFF);

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
        final Map<Bid, List<Optional<Reason>>> reasonChains = new HashMap<>();
        final List<TournamentResultEntry> leftInGroup = groupOrdered.stream()
                .filter(e -> {
                    if (playOffResult.getPlayOffBids()
                            .contains(e.getUser().getBid())) {
                        reasonChains.put(e.getUser().getBid(), e.getReasonChain());
                        return false;
                    }
                    return true;
                })
                .collect(toList());

        playOffResult.getEntries().forEach(
                e -> e.getReasonChain().addAll(reasonChains.get(e.getUser().getBid())));
        playOffResult.getEntries().addAll(leftInGroup);
    }

    public TournamentComplete completeInfo(TournamentMemState tournament) {
        return TournamentComplete
                .builder()
                .state(tournament.getState())
                .name(tournament.getName())
                .categories(tournament.categoryLinks())
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

    @Inject
    private SuggestionService suggestionService;

    public Bid enlistOffline(Uid adminUid, TournamentMemState tournament,
            EnlistOffline enlistment, DbUpdater batch) {
        validateEnlistOffline(tournament, enlistment);
        final UserInfo uInfo = userDao.getUserByUid(enlistment.getUid())
                .orElseThrow(() -> notFound("user not found"));
        final Bid participantId = tournament.allocateBidFor(
                enlistment.getCid(), enlistment.getUid());

        final Instant now = clocker.get();
        if (tournament.getState() == Open && !enlistment.getGroupId().isPresent()) {
            enlistment.setGroupId(Optional.of(
                    castingLotsService.addGroup(
                            selectCid(enlistment.getCid(), tournament, batch))));
        }
        tournament.registerParticipant(
                ParticipantMemState.builder()
                        .bidState(enlistment.getBidState())
                        .enlistedAt(now)
                        .updatedAt(now)
                        .name(uInfo.getName())
                        .cid(enlistment.getCid())
                        .uid(enlistment.getUid())
                        .bid(participantId)
                        .gid(enlistment.getGroupId())
                        .tid(tournament.getTid())
                        .build());
        enlist(tournament, participantId, enlistment.getProvidedRank(),
                batch, enlistment.getGroupId());

        suggestionService.createSuggestion(adminUid, uInfo.getName(), uInfo.getUid(), batch);

        return participantId;
    }

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> tournamentRelations;

    @SneakyThrows
    @Transactional(TRANSACTION_MANAGER)
    public Tid copy(CopyTournament copyTournament) {
        final Tid masterCopyTid = tournamentDao.copy(copyTournament);
        categoryDao.copy(copyTournament.getOriginTid(), masterCopyTid);
        final RelatedTids relatedTids = tournamentRelations
                .get(copyTournament.getOriginTid());
        relatedTids.getChildren().forEach((type, childTid) -> {
            log.info("Copy console {} tid {}", type, childTid);
            final Tid consoleCopyTid = copy(copyTournament.withOriginTid(childTid));
            tournamentDao.createRelation(masterCopyTid, consoleCopyTid, type);
        });
        return masterCopyTid;
    }

    public PlayOffMatches playOffMatches(TournamentMemState tournament, Cid cid) {
        return playOffService.playOffMatches(tournament, cid, Optional.empty());
    }

    public List<TournamentDigest> findFollowingFrom(Tid tid) {
        return tournamentDao.findFollowingFrom(tid);
    }
}
