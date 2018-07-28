package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.category.CategoryState.Ply;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.category.CategoryMemState;
import org.dan.ping.pong.app.category.CategoryService;
import org.dan.ping.pong.app.category.CategoryState;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class TournamentTerminator {
    @Inject
    private CategoryService categoryService;

    @Inject
    private TournamentDao tournamentDao;

    @Inject
    private Clocker clocker;

    public void tryToEndOfTournamentCategory(
            TournamentMemState tournament, Cid cid, DbUpdater batch) {
        final CategoryMemState cat = tournament.getCategory(cid);
        if (cat.getState() == CategoryState.End) {
            return;
        }
        if (cat.getState() != Ply) {
            throw internalError("Attempt to end cid " + cid
                    + " in tid " + tournament.getTid()
                    + " not in play but " + cat.getState());
        }
        categoryService.markComplete(tournament, cat, batch);
        final Tid tid = tournament.getTid();
        log.info("Tid {} complete in cid {}", tid, cid);
        final List<Cid> incompleteCids = categoryService.findIncompleteCategories(tournament);
        if (incompleteCids.isEmpty()) {
            log.info("All matches of tid {} are complete", tid);
            setTournamentCompleteAt(tournament, clocker.get(), batch);
            setTournamentState(tournament, Close, batch);
        } else {
            log.info("Tid {} is not fully complete due cats {}", tid, incompleteCids);
        }
    }

    public void setTournamentState(
            TournamentMemState tournament, TournamentState target, DbUpdater batch) {
        log.info("Switch tid {} from state {} to {}",
                tournament.getTid(), tournament.getState(), target);
        if (tournament.getState() != target) {
            tournament.setState(target);
            tournamentDao.setState(tournament, batch);
        }
    }

    public void setTournamentCompleteAt(TournamentMemState tournament, Instant now, DbUpdater batch) {
        tournament.setCompleteAt(Optional.of(now));
        tournamentDao.setCompleteAt(tournament.getTid(), tournament.getCompleteAt(), batch);
    }

    public void completeAllOpenCategories(TournamentMemState tournament, DbUpdater batch) {
        tournament.getCategories().values().stream()
                .filter(c -> c.getState() == Ply)
                .forEach(c -> tryToEndOfTournamentCategory(tournament, c.getCid(), batch));
    }
}
