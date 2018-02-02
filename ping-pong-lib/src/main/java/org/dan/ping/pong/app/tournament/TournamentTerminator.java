package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.tournament.TournamentState.Close;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.category.CategoryService;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class TournamentTerminator {
    @Inject
    private CategoryService categoryService;

    @Inject
    private TournamentDao tournamentDao;

    @Inject
    private Clocker clocker;

    public boolean endOfTournamentCategory(TournamentMemState tournament, int cid, DbUpdater batch) {
        final Tid tid = tournament.getTid();
        log.info("Tid {} complete in cid {}", tid, cid);
        Set<Integer> incompleteCids = categoryService.findIncompleteCategories(tournament);
        if (incompleteCids.isEmpty()) {
            log.info("All matches of tid {} are complete", tid);
            setTournamentCompleteAt(tournament, clocker.get(), batch);
            setTournamentState(tournament, Close, batch);
            return true;
        } else {
            log.info("Tid {} is fully complete", tid);
            return false;
        }
    }

    public void setTournamentState(TournamentMemState tournament, TournamentState target, DbUpdater batch) {
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
}
