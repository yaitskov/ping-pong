package org.dan.ping.pong.app.tournament;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.tournament.CumulativeScore.BEST_ORDER;
import static org.dan.ping.pong.app.tournament.TournamentState.Announce;
import static org.dan.ping.pong.app.tournament.TournamentState.Canceled;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentState.Hidden;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.castinglots.CastingLotsService;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.category.CategoryInfo;
import org.dan.ping.pong.app.category.CategoryService;
import org.dan.ping.pong.app.group.BidSuccessInGroup;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.Pid;
import org.dan.ping.pong.app.place.PlaceService;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.table.TableService;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.app.user.UserRegRequest;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class TournamentService {
    private static final ImmutableSet<TournamentState> EDITABLE_STATES = ImmutableSet.of(Hidden, Announce, Draft);
    private static final ImmutableSet<TournamentState> CONFIGURABLE_STATES = EDITABLE_STATES;
    private static final int DAYS_TO_SHOW_COMPLETE_BIDS = 30;

    @Inject
    private TournamentDao tournamentDao;

    @Transactional(TRANSACTION_MANAGER)
    public int create(int uid, CreateTournament newTournament) {
        return tournamentDao.create(uid, newTournament);
    }

    public void enlistOnline(EnlistTournament enlistment,
            OpenTournamentMemState tournament, UserInfo user, DbUpdater batch) {
        if (tournament.getState() != Draft) {
            throw badRequest("Tournament is not in Draft but "
                    + tournament.getState());
        }
        log.info("Uid {} enlists to tid {} in cid {}",
                user.getUid(), tournament.getTid(), enlistment.getCategoryId());
        int uid = user.getUid();
        tournament.getParticipants().put(uid, ParticipantMemState.builder()
                .bidState(Want)
                .uid(new Uid(uid))
                .name(user.getName())
                .cid(enlistment.getCategoryId())
                .tid(new Tid(tournament.getTid()))
                .build());
        enlist(tournament, uid, batch);
    }

    private void enlist(OpenTournamentMemState tournament, int uid, DbUpdater batch) {
        if (tournament.getState() != TournamentState.Draft) {
            throw badRequest(BadStateError.of(tournament.getState(),
                    "Tournament is not in a draft state"));
        }
        bidDao.enlist(tournament.getParticipant(uid), clocker.get(), batch);
    }

    private void ensureDrafting(TournamentState state) {
        if (state != TournamentState.Draft && state != Open) {
            throw badRequest(BadStateError.of(state,
                    "Tournament is not in a valid state"));
        }
    }

    public List<TournamentDigest> findInWithEnlisted(int uid, int days) {
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
    private TableService tableService;

    @Inject
    private Clocker clocker;

    public void begin(OpenTournamentMemState tournament, DbUpdater batch) {
        if (tournament.getState() != TournamentState.Draft) {
            throw badRequest("Tournament " + tournament.getTid()
                    + " is not in draft state but "
                    + tournament.getState());
        }
        castingLotsService.makeGroups(tournament);
        tournament.setState(TournamentState.Open);
        tournamentDao.setState(tournament, batch);
        final Instant now = clocker.get();
        findReadyToStartTournamentBid(tournament).forEach(bid -> {
            bidService.setBidState(bid, BidState.Wait, singletonList(bid.getBidState()), batch);
        });
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> {
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    return tableService.scheduleFreeTables(tournament, place, now, batch);
                });
    }

    private List<ParticipantMemState> findReadyToStartTournamentBid(OpenTournamentMemState tournament) {
        return tournament.getParticipants().values()
                .stream()
                .filter(bid -> bid.getBidState() == Here)
                .collect(toList());
    }

    @Inject
    private CategoryDao categoryDao;

    public DraftingTournamentInfo getDraftingTournament(int tid,
            Optional<Integer> participantId) {
        final List<CategoryInfo> categories = categoryDao.listCategoriesByTid(tid);
        final DraftingTournamentInfo result = tournamentDao
                .getDraftingTournament(tid, participantId)
                .orElseThrow(() -> notFound("Tournament " + tid + " not found"));
        ensureDrafting(result.getState());
        result.setCategories(categories);
        return result;
    }

    public MyTournamentInfo getMyTournamentInfo(int tid) {
        return tournamentDao.getMyTournamentInfo(tid)
                .orElseThrow(() -> notFound("Tournament "
                        + tid + " not found"));
    }

    public void leaveTournament(ParticipantMemState bid, OpenTournamentMemState tournament,
            BidState targetState, DbUpdater batch) {
        final int tid = tournament.getTid();
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

    private void leaveOpenTournament(ParticipantMemState bid, OpenTournamentMemState tournament,
            BidState targetState, DbUpdater batch) {
        final int tid = tournament.getTid();
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
    private TableDao tableDao;

    @Inject
    private SequentialExecutor sequentialExecutor;

    @Inject
    private PlaceService placeCache;

    @Inject
    private BidService bidService;

    @Value("${match.score.timeout}")
    private int matchScoreTimeout;

    public void activeParticipantLeave(ParticipantMemState bid, OpenTournamentMemState tournament,
            Instant now, BidState target, DbUpdater batch) {
        final int uid = bid.getUid().getId();
        List<MatchInfo> incompleteMy = matchService.bidIncompleteGroupMatches(uid, tournament);
        log.info("activeParticipantLeave uid {} incomplete {}", uid, incompleteMy.size());
        if (incompleteMy.isEmpty()) {
            leaveFromPlayOff(bid, tournament, batch);
            if (bid.getBidState() != Win2 || target == Expl) {
                bidService.setBidState(bid, target, singletonList(bid.getBidState()), batch);
            }
        } else {
            for (MatchInfo match : incompleteMy) {
                matchService.walkOver(tournament, uid, match, batch);
            }
            bidService.setBidState(bid, target, singletonList(bid.getBidState()), batch);
        }
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> {
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    return tableService.scheduleFreeTables(tournament, place, now, batch);
                });
    }

    private void leaveFromPlayOff(ParticipantMemState bid, OpenTournamentMemState tournament, DbUpdater batch) {
        matchService.playOffMatchForResign(bid.getUid().getId(), tournament)
                .ifPresent(match -> {
                    matchService.walkOver(tournament, bid.getUid().getId(), match, batch);
                    leaveFromPlayOff(bid, tournament, batch);
                });
    }

    public void setTournamentState(OpenTournamentMemState tournament, DbUpdater batch) {
        tournamentDao.setState(tournament, batch);
    }

    public void setTournamentCompleteAt(OpenTournamentMemState tournament, Instant now, DbUpdater batch) {
        tournament.setCompleteAt(Optional.of(now));
        tournamentDao.setCompleteAt(tournament.getTid(), tournament.getCompleteAt(), batch);
    }

    public List<OpenTournamentDigest> findRunning(int completeInLastDays) {
        return tournamentDao.findRunning(
                clocker.get().minus(completeInLastDays, DAYS));
    }

    public MyRecentTournaments findMyRecentTournaments(int uid) {
        return tournamentDao.findMyRecentTournaments(
                clocker.get().minus(DAYS_TO_SHOW_COMPLETE_BIDS, DAYS),
                uid);
    }

    public MyRecentJudgedTournaments findMyRecentJudgedTournaments(int uid) {
        return tournamentDao.findMyRecentJudgedTournaments(clocker.get(), uid);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void update(OpenTournamentMemState tournament, TournamentUpdate update, DbUpdater batch) {
        if (!EDITABLE_STATES.contains(tournament.getState())) {
            throw badRequest("Tournament could be modified until it's open");
        }
        tournament.setPid(new Pid(update.getPlaceId()));
        tournamentDao.update(update, batch);
    }

    public TournamentParameters getTournamentParams(int tid) {
        return tournamentDao.getTournamentParams(tid)
                .orElseThrow(() -> notFound("Tournament does not exist"));
    }

    public void updateTournamentParams(OpenTournamentMemState tournament,
            TournamentParameters parameters, DbUpdater batch) {
        validate(parameters);
        if (!CONFIGURABLE_STATES.contains(tournament.getState())) {
            throw badRequest("Tournament could be modified until it's open");
        }
        final TournamentRules rules = tournament.getRule();
        rules.getGroup().setQuits(parameters.getQuitsGroup());
        rules.getGroup().setMaxSize(parameters.getMaxGroupSize());
        rules.getMatch().setSetsToWin(parameters.getMatchScore());
        rules.setThirdPlaceMatch(parameters.getThirdPlaceMatch());
        tournamentDao.updateParams(tournament.getTid(), rules, batch);
    }

    private void validate(TournamentParameters parameters) {
        if (parameters.getMatchScore() < 1) {
            throw badRequest("Match score is less than 1");
        }
        if (parameters.getQuitsGroup() < 1) {
            throw badRequest("Quits from group is less than 1");
        }
        if (parameters.getMaxGroupSize() < 2 || parameters.getMaxGroupSize() > 20) {
            throw badRequest("Max group size is out of range");
        }
        if (parameters.getMaxGroupSize() <= parameters.getQuitsGroup()) {
            throw badRequest("Max group size is less than quits from group");
        }
    }

    @Inject
    private MatchDao matchDao;

    @Inject
    private GroupDao groupDao;

    public void cancel(OpenTournamentMemState tournament, DbUpdater batch) {
        final int tid = tournament.getTid();
        tournament.setState(Canceled);
        setTournamentState(tournament, batch);
        setTournamentCompleteAt(tournament, clocker.get(), batch);
        matchDao.deleteAllByTid(tournament, batch, tournament.getMatches().size());
        final Set<Integer> mids = new HashSet<>(tournament.getMatches().keySet());
        tournament.getMatches().clear();
        tournament.getGroups().clear();
        groupDao.deleteAllByTid(tournament.getTid(), batch, tournament.getGroups().size());
        final Instant now = clocker.get();
        tournament.getParticipants().values().stream()
                .filter(bid -> bid.getState() != Quit)
                .forEach(bid -> bid.setBidState(Want));
        bidDao.resetStateByTid(tid, now, batch);
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()), place -> {
            batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
            tableService.freeTables(place, mids, batch);
            return null;
        });
    }

    @Inject
    private PlayOffService playOffService;

    @Inject
    private GroupService groupService;

    public List<TournamentResultEntry> tournamentResult(OpenTournamentMemState tournament, int cid) {
        final List<MatchInfo> cidMatches = categoryService.findMatchesInCategory(tournament, cid);
        int level = 1;
        final Map<Integer, CumulativeScore> uidLevel = new HashMap<>();
        Collection<MatchInfo> baseMatches = playOffService.findBaseMatches(cidMatches);
        final MatchValidationRule matchRules = tournament.getRule().getMatch();
        while (true) {
            ranksLevelMatches(tournament, level++, uidLevel, baseMatches, matchRules);
            final Collection<MatchInfo> nextLevel = playOffService
                    .findNextMatches(tournament.getMatches(), baseMatches);
            if (nextLevel.isEmpty()) {
                break;
            }
            baseMatches = nextLevel;
        }
        ranksLevelMatches(tournament, 0, uidLevel, playOffService.findGroupMatches(cidMatches), matchRules);
        return uidLevel.values().stream().sorted(BEST_ORDER)
                .map(cuScore -> {
                    final ParticipantMemState participant = tournament.getParticipant(cuScore.getRating().getUid());
                    return TournamentResultEntry.builder()
                            .user(participant.toLink())
                            .state(participant.getState())
                            .punkts(cuScore.getRating().getPunkts())
                            .score(cuScore)
                        .build();
                })
                .collect(toList());
    }

    private void ranksLevelMatches(OpenTournamentMemState tournament, int level,
            Map<Integer, CumulativeScore> uidLevel,
            Collection<MatchInfo> matches, MatchValidationRule rules) {
        final Map<Integer, BidSuccessInGroup> uid2Stat = groupService.emptyMatchesState(tournament, matches);
        matches.forEach(m -> groupService.aggMatch(uid2Stat, m, rules));
        uid2Stat.forEach((uid, stat) ->
                uidLevel.merge(uid,
                    CumulativeScore.builder()
                            .level(level)
                            .rating(stat)
                            .weighted(stat.multiply((int) Math.pow(10, level)))
                            .build(),
                    CumulativeScore::merge));
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public TournamentComplete completeInfo(int tid) {
        TournamentComplete result = tournamentDao.completeInfo(tid)
                .orElseThrow(() -> notFound("Tournament has been not found"));
        result.setCategories(categoryDao.listCategoriesByTid(tid));
        return result;
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<TournamentDigest> findWritableForAdmin(int uid, int days) {
        return tournamentDao.findWritableForAdmin(uid,
                clocker.get().minus(days, DAYS));
    }

    @Inject
    private UserDao userDao;

    public int enlistOffline(OpenTournamentMemState tournament,
            EnlistOffline enlistment, DbUpdater batch) {
        if (tournament.getState() != Draft) {
            throw badRequest("Tournament is not in Draft but "
                    + tournament.getState());
        }
        final int participantUid = userDao.register(UserRegRequest.builder()
                .name(enlistment.getName())
                .build());
        tournament.getParticipants().put(participantUid, ParticipantMemState.builder()
                .bidState(enlistment.getBidState())
                .name(enlistment.getName())
                .cid(enlistment.getCid())
                .uid(new Uid(participantUid))
                .tid(new Tid(tournament.getTid()))
                .build());
        enlist(tournament, participantUid, batch);
        return participantUid;
    }

    @Transactional(TRANSACTION_MANAGER)
    public int copy(CopyTournament copyTournament) {
        final int tid = tournamentDao.copy(copyTournament);
        categoryDao.copy(copyTournament.getOriginTid(), tid);
        return tid;
    }

    @Inject
    private CategoryService categoryService;

    public boolean endOfTournamentCategory(OpenTournamentMemState tournament, int cid, DbUpdater batch) {
        int tid = tournament.getTid();
        log.info("Tid {} complete in cid {}", tid, cid);
        Set<Integer> incompleteCids = categoryService.findIncompleteCategories(tournament);
        if (incompleteCids.isEmpty()) {
            log.info("All matches of tid {} are complete", tid);
            setTournamentCompleteAt(tournament, clocker.get(), batch);
            tournament.setState(Close);
            setTournamentState(tournament, batch);
            return true;
        } else {
            log.info("Tid {} is fully complete", tid);
            return false;
        }
    }
}
