package org.dan.ping.pong.app.tournament;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.tournament.TournamentState.Announce;
import static org.dan.ping.pong.app.tournament.TournamentState.Canceled;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentState.Hidden;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.castinglots.CastingLotsService;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.category.CategoryInfo;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.GroupMatchForResign;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.table.TableService;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.app.user.UserRegRequest;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class TournamentService {
    private static final ImmutableSet<TournamentState> EDITABLE_STATES = ImmutableSet.of(Hidden, Announce, Draft);
    private static final ImmutableSet<TournamentState> CONFIGURABLE_STATES = EDITABLE_STATES;

    @Inject
    private TournamentDao tournamentDao;

    @Transactional(TRANSACTION_MANAGER)
    public int create(int uid, CreateTournament newTournament) {
        return tournamentDao.create(uid, newTournament);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void enlist(int uid, EnlistTournament enlistment) {
        final MyTournamentInfo info = tournamentDao.getMyTournamentInfo(enlistment.getTid())
                 .orElseThrow(() -> notFound("Tournament "
                         + enlistment.getTid() + " does not exist"));
        ensureDrafting(info.getState());
        bidDao.enlist(uid, enlistment);
    }

    private void ensureDrafting(TournamentState state) {
        if (state != TournamentState.Draft && state != Open) {
            throw badRequest(BadStateError.of(state,
                    "Tournament is not in a valid state"));
        }
    }

    public List<TournamentDigest> findInWithEnlisted(int uid, int days) {
        return tournamentDao.findEnlistedIn(uid,
                clocker.get().minus(days, ChronoUnit.DAYS));
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

    @Transactional(TRANSACTION_MANAGER)
    public void begin(int tid) {
        castingLotsService.makeGroups(tid);
        bidDao.casByTid(BidState.Here, BidState.Wait, tid);
        tableService.scheduleFreeTables(tid, clocker.get());
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

    @Transactional(TRANSACTION_MANAGER)
    public void leaveTournament(int uid, int tid, BidState targetState) {
        log.info("User {} leaves tournament {} as {}", uid, tid, targetState);
        final TournamentInfo tinfo = tournamentDao.getById(tid)
                .orElseThrow(() -> notFound("Tournament {} not found"));
        final TournamentState state = tinfo.getState();
        switch (state) {
            case Close:
            case Canceled:
            case Replaced:
                throw badRequest("Tournament is complete");
            case Open:
                leaveOpenTournament(uid, tid, targetState);
                break;
            case Hidden:
            case Announce:
            case Draft:
                bidDao.resign(uid, tid, targetState);
                break;
            default:
                throw internalError("State " + state + " is not supported");
        }
    }

    private void leaveOpenTournament(int uid, int tid, BidState targetState) {
        log.info("User {} leaves open tournament {}", uid, tid);
        final BidState state = bidDao.getState(tid, uid)
                .orElseThrow(() -> notFound("User does participant in the tournament"));
        switch (state) {
            case Play:
            case Rest:
            case Wait:
                activeParticipantLeave(uid, tid, clocker.get(), targetState);
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
                bidDao.setBidState(tid, uid, state, targetState);
                break;
            default:
                throw internalError("Unknown state " + state);
        }
    }

    @Inject
    private MatchService matchService;

    @Inject
    private TableDao tableDao;

    public void activeParticipantLeave(int uid, int tid, Instant now, BidState target) {
        List<GroupMatchForResign> list = matchDao.groupMatchesOfParticipant(uid, tid);
        final Map<Integer, GroupMatchForResign> completeMy = new HashMap<>();
        final Map<Integer, GroupMatchForResign> incompleteMy = new HashMap<>();
        int gid = groupMatches(list, completeMy, incompleteMy);
        log.info("activeParticipantLeave uid {} incomplete {}", uid, incompleteMy.size());
        if (incompleteMy.isEmpty()) {
            leaveFromPlayOff(uid, tid, now, target);
        } else {
            for (GroupMatchForResign match : incompleteMy.values()) {
                matchDao.complete(uid, match, now);
                if (match.getState() == Game) {
                    tableDao.freeTable(match.getMid());
                    bidDao.setBidState(tid, match.getOpponentUid(), BidState.Play, BidState.Wait);
                }
            }
            bidDao.resign(uid, tid, target);
            matchService.tryToCompleteGroup(gid, tid, emptySet());
            matchService.autoCompletePlayOffHalfMatches(tid);
            tableService.scheduleFreeTables(tid, now);
        }
    }

    private void leaveFromPlayOff(int uid, int tid, Instant now, BidState target) {
        Optional<PlayOffMatchForResign> playOffMatch = matchDao.playOffMatchForResign(uid, tid);
        if (playOffMatch.isPresent()) {
            boolean schedule = matchService.completePlayOffMatch(uid, tid, target, playOffMatch.get());
            if (playOffMatch.get().getState() == Game) {
                tableDao.freeTable(playOffMatch.get().getMid());
            }
            if (schedule) {
                matchService.autoCompletePlayOffHalfMatches(tid);
                tableService.scheduleFreeTables(tid, now);
            }
        } else { // play off is not begun yet
            bidDao.resign(uid, tid, target);
        }
    }

    private int groupMatches(List<GroupMatchForResign> list,
            Map<Integer, GroupMatchForResign> completeMy,
            Map<Integer, GroupMatchForResign> incompleteMy) {
        int gid = 0;
        for(GroupMatchForResign match : list) {
            gid = match.getGid();
            if (match.getState() == Over) {
                completeMy.put(match.getMid(), match);
            } else {
                incompleteMy.put(match.getMid(), match);
            }
        }
        return gid;
    }

    @Transactional(TRANSACTION_MANAGER)
    public void setTournamentStatus(int uid, SetTournamentState stateUpdate) {
        // check permissions
        tournamentDao.setState(stateUpdate.getTid(), stateUpdate.getState(), clocker.get());
    }

    public List<OpenTournamentDigest> findRunning(int completeInLastDays) {
        return tournamentDao.findRunning(
                clocker.get().minus(completeInLastDays, ChronoUnit.DAYS));
    }

    public MyRecentTournaments findMyRecentTournaments(int uid) {
        return tournamentDao.findMyRecentTournaments(clocker.get(), uid);
    }

    public MyRecentJudgedTournaments findMyRecentJudgedTournaments(int uid) {
        return tournamentDao.findMyRecentJudgedTournaments(clocker.get(), uid);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void update(int uid, TournamentUpdate update) {
        if (tournamentDao.isAdminOf(uid, update.getTid())) {
            final TournamentState state = tournamentDao.getById(update.getTid())
                    .orElseThrow(() -> notFound("Tournament does not exist"))
                    .getState();
            if (!EDITABLE_STATES.contains(state)) {
                throw badRequest("Tournament could be modified until it's open");
            }
            tournamentDao.update(update);
        } else {
            throw forbidden("No write access to the tournament");
        }
    }

    public TournamentParameters getTournamentParams(int tid) {
        return tournamentDao.getTournamentParams(tid)
                .orElseThrow(() -> notFound("Tournament does not exist"));
    }

    @Transactional(TRANSACTION_MANAGER)
    public void updateTournamentParams(int uid, TournamentParameters parameters) {
        validate(parameters);
        if (tournamentDao.isAdminOf(uid, parameters.getTid())) {
            final TournamentState state = tournamentDao.getById(parameters.getTid())
                    .orElseThrow(() -> notFound("Tournament does not exist"))
                    .getState();
            if (!CONFIGURABLE_STATES.contains(state)) {
                throw badRequest("Tournament could be modified until it's open");
            }
            tournamentDao.updateParams(parameters);
        } else {
            throw forbidden("No write access to the tournament");
        }
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

    @Transactional(TRANSACTION_MANAGER)
    public void cancel(int uid, int tid) {
        if (tournamentDao.isAdminOf(uid, tid)) {
            tournamentDao.setState(tid, Canceled, clocker.get());
            matchDao.deleteAllByTid(tid);
            bidDao.resetStateByTid(tid);
            tableService.freeTables(tid);
        } else {
            throw forbidden("No write access to the tournament");
        }
    }

    public List<TournamentResultEntry> tournamentResult(int tid, int cid) {
        return tournamentDao.tournamentResult(tid, cid);
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
                clocker.get().minus(days, ChronoUnit.DAYS));
    }

    @Inject
    private UserDao userDao;

    @Transactional(TRANSACTION_MANAGER)
    public int enlistOffline(EnlistOffline enlistment) {
        final MyTournamentInfo myTournamentInfo = getMyTournamentInfo(enlistment.getTid());
        if (myTournamentInfo.getState() != Draft) {
            throw badRequest("Tournament is not in Draft but "
                    + myTournamentInfo.getState());
        }
        final int participantUid = userDao.register(UserRegRequest.builder()
                .name(enlistment.getName())

                .build());
        enlist(participantUid,
                EnlistTournament.builder()
                        .categoryId(enlistment.getCid())
                        .tid(enlistment.getTid())
                        .build());
        if (enlistment.getBidState() == Here || enlistment.getBidState() == Paid) {
            bidDao.setBidState(enlistment.getTid(), participantUid, Want, enlistment.getBidState());
        }
        return participantUid;
    }
}
