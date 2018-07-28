package org.dan.ping.pong.app.sched;

import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentMemState.PLACE_IS_BUSY;
import static org.dan.ping.pong.app.tournament.TournamentMemState.TID;
import static org.dan.ping.pong.app.tournament.TournamentState.TERMINAL_STATE;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.place.PlaceService;
import org.dan.ping.pong.app.table.TableService;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class GlobalScheduleService implements ScheduleService {
    @Inject
    private SequentialExecutor sequentialExecutor;

    @Inject
    private PlaceService placeCache;

    @Inject
    private TableService tableService;

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> tournamentRelatedCache;

    @SneakyThrows
    private boolean notParent(Tid busyTid, TournamentMemState tournament) {
        return !Optional.of(busyTid)
                .equals(tournamentRelatedCache.get(tournament.getTid())
                        .parentTidO());
    }

    @Override
    public void beginTournament(TournamentMemState tournament,
            DbUpdater batch, Instant now) {
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> {
                    place.getHostingTid()
                            .filter(busyTid -> notParent(busyTid, tournament))
                            .ifPresent(busyTid -> {
                                throw badRequest(PLACE_IS_BUSY, TID, busyTid);
                            });
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    tableService.bindPlace(place, batch, Optional.of(tournament.getTid()));
                    return tableService.scheduleFreeTables(tournament, place, now, batch);
                });
    }

    @Override
    public void cancelTournament(TournamentMemState tournament,
            DbUpdater batch, Instant now) {
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()), place -> {
            batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
            tableService.bindPlace(place, batch, Optional.empty());
            tableService.freeTables(place,
                    new HashSet<>(tournament.getMatches().keySet()), batch);
            return null;
        });
    }

    @Override
    public void participantLeave(TournamentMemState tournament,
            DbUpdater batch, Instant now) {
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> {
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    return tableService.scheduleFreeTables(tournament, place, now, batch);
                });
    }

    @Override
    public void afterMatchComplete(TournamentMemState tournament,
            DbUpdater batch, Instant now) {
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> {
                    log.info("Schedule tournament {}", tournament.getTid());
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    if (TERMINAL_STATE.contains(tournament.getState())) {
                        tableService.bindPlace(place, batch, Optional.empty());
                    }
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    return tableService.scheduleFreeTables(tournament, place, now, batch);
                });
    }

    @Override
    public <T> T withPlaceTables(TournamentMemState tournament, Function<TablesDiscovery, T> f) {
        return sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> f.apply(new GlobalTablesDiscovery(place)));
    }
}
