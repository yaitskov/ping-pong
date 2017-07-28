package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.tournament.TournamentState.Announce;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentState.Hidden;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;
import static org.dan.ping.pong.sys.error.PiPoEx.notAuthorized;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.castinglots.CastingLotsService;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.category.CategoryInfo;
import org.dan.ping.pong.app.table.TableService;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

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
        if (state != TournamentState.Draft) {
            throw badRequest(BadStateError.of(state,
                    "Tournament is not in a valid state"));
        }
    }

    public List<DatedTournamentDigest> findInWithEnlisted(int uid) {
        return tournamentDao.findEnlistedIn(uid);
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

    public void resign(int uid, Integer tid) {
        bidDao.resign(uid, tid);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void setTournamentStatus(int uid, SetTournamentState stateUpdate) {
        // check permissions
        tournamentDao.setState(stateUpdate.getTid(), stateUpdate.getState());
    }

    public List<OpenTournamentDigest> findRunning() {
        return tournamentDao.findRunning();
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
}
