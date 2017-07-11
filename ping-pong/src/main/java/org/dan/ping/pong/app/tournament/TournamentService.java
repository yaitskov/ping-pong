package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badState;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

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
    @Inject
    private TournamentDao tournamentDao;

    @Transactional(TRANSACTION_MANAGER)
    public int create(int uid, CreateTournament newTournament) {
        return tournamentDao.create(uid, newTournament);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void enlist(int uid, EnlistTournament enlistment) {
        bidDao.enlist(uid, enlistment);
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
        if (result.getState() != TournamentState.Draft) {
            throw badState(result.getState());
        }
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
}
