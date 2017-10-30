package org.dan.ping.pong.app.sched;

import static org.dan.ping.pong.app.tournament.TournamentService.PLACE_IS_BUSY;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;
import static org.dan.ping.pong.app.tournament.TournamentState.Canceled;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.app.tournament.TournamentState.Replaced;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.place.PlaceService;
import org.dan.ping.pong.app.table.TableService;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;

public class GlobalScheduleService implements ScheduleService {
    public static final Set<TournamentState> TERMINAL_STATE = ImmutableSet.of(Close, Canceled, Replaced);

    @Inject
    private SequentialExecutor sequentialExecutor;

    @Inject
    private PlaceService placeCache;

    @Inject
    private TableService tableService;

    @Override
    public void beginTournament(OpenTournamentMemState tournament,
            DbUpdater batch, Instant now) {
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> {
                    place.getHostingTid().ifPresent(busyTid -> {
                        throw badRequest(PLACE_IS_BUSY, TID, busyTid);
                    });
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    tableService.bindPlace(place, batch, Optional.of(tournament.getTid()));
                    return tableService.scheduleFreeTables(tournament, place, now, batch);
                });
    }

    @Override
    public void cancelTournament(OpenTournamentMemState tournament,
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
    public void participantLeave(OpenTournamentMemState tournament,
            DbUpdater batch, Instant now) {
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> {
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    return tableService.scheduleFreeTables(tournament, place, now, batch);
                });
    }

    @Override
    public void afterMatchComplete(OpenTournamentMemState tournament,
            DbUpdater batch, Instant now) {
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> {
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    if (GlobalScheduleService.TERMINAL_STATE.contains(tournament.getState())) {
                        tableService.bindPlace(place, batch, Optional.empty());
                    }
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    return tableService.scheduleFreeTables(tournament, place, now, batch);
                });
    }

    @Override
    public <T> T withPlace(OpenTournamentMemState tournament, Function<TablesDiscovery, T> f) {
        return sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> f.apply(new GlobalTablesDiscovery(place)));
    }
}