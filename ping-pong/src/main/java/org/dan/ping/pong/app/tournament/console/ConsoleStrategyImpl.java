package org.dan.ping.pong.app.tournament.console;

import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.sched.ScheduleCtx.SCHEDULE_SELECTOR;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.CategoryService;
import org.dan.ping.pong.app.sched.ScheduleService;
import org.dan.ping.pong.app.tournament.EnlistTournament;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentCache;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class ConsoleStrategyImpl implements ConsoleStrategy {
    @Inject
    private TournamentService tournamentService;

    @Inject
    private CategoryService categoryService;

    @Inject
    private TournamentCache tournamentCache;

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    public LoadingCache<Tid, RelatedTids> tournamentRelationCache;

    private int findCidOrCreate(TournamentMemState tournament, int gid,
            TournamentMemState consoleTournament, DbUpdater batch) {
        final int masterCid = tournament.getGroup(gid).getCid();
        final String categoryName = tournament.getCategory(masterCid).getName();
        final Optional<Integer> oCid = consoleTournament.findCidByName(categoryName);
        if (oCid.isPresent()) {
            return oCid.get();
        }
        return categoryService.createCategory(consoleTournament, categoryName, batch);
    }

    @Inject
    @Named(SCHEDULE_SELECTOR)
    private ScheduleService scheduleService;

    @Inject
    private Clocker clocker;

    @Override
    @SneakyThrows
    public void onGroupComplete(
            int gid, TournamentMemState masterTournament,
            Set<Uid> loserUids, DbUpdater batch) {
        final RelatedTids relatedTids = tournamentRelationCache.get(masterTournament.getTid());

        final TournamentMemState consoleTournament = tournamentCache.load(
                relatedTids.getChild().orElseThrow(() -> internalError("tournament  "
                        + masterTournament.getTid() + " has no console tournament")));

        final int cid = findCidOrCreate(masterTournament, gid, consoleTournament, batch);

        batch.onFailure(() -> tournamentCache.invalidate(consoleTournament.getTid()));
        log.info("Enlist loser bids {} to console tournament {}",
                loserUids, consoleTournament.getTid());
        consoleTournament.setState(Draft);
        loserUids.stream().map(masterTournament::getParticipant).forEach(bid ->
                        tournamentService.enlistOnlineWithoutValidation(
                                EnlistTournament.builder()
                                        .categoryId(cid)
                                        .bidState(Here)
                                        .providedRank(Optional.empty())
                                        .build(),
                                consoleTournament, bid, batch));
        if (!areAllGroupMatchesOver(masterTournament)) {
            return;
        }
        log.info("All group matches of tid {} are over, so begin console tournament {}",
                masterTournament.getTid(), consoleTournament.getTid());
        tournamentService.begin(consoleTournament, batch);
        masterTournament.getCondActions().getOnScheduleTables().add(
                () -> {
                    log.info("Schedule console tournament {}", consoleTournament.getTid());
                    scheduleService.beginTournament(consoleTournament, batch, clocker.get());
                });
    }

    private boolean areAllGroupMatchesOver(TournamentMemState tournament) {
        return !tournament.getMatches()
                .values().stream()
                .anyMatch(m -> m.getGid().isPresent() && m.getState() != Over);
    }
}
