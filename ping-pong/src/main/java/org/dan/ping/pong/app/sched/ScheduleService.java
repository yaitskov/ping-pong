package org.dan.ping.pong.app.sched;

import static org.dan.ping.pong.app.tournament.TournamentService.PLACE_IS_BUSY;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;
import static org.dan.ping.pong.app.tournament.TournamentState.Canceled;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.app.tournament.TournamentState.Replaced;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.ImmutableSet;
import ord.dan.ping.pong.jooq.tables.Place;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.place.PlaceMemState;
import org.dan.ping.pong.app.place.PlaceService;
import org.dan.ping.pong.app.table.TableService;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Inject;

public class ScheduleService {
    public static final Set<TournamentState> TERMINAL_STATE = ImmutableSet.of(Close, Canceled, Replaced);

    @Inject
    private SequentialExecutor sequentialExecutor;

    @Inject
    private PlaceService placeCache;

    @Inject
    private TableService tableService;

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

    public void cancelTournament(OpenTournamentMemState tournament,
            DbUpdater batch, Set<Integer> mids) {
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()), place -> {
            batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
            tableService.bindPlace(place, batch, Optional.empty());
            tableService.freeTables(place, mids, batch);
            return null;
        });
    }

    public void participantLeave(OpenTournamentMemState tournament,
            DbUpdater batch, Instant now) {
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> {
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    return tableService.scheduleFreeTables(tournament, place, now, batch);
                });
    }

    public void afterMatchComplete(OpenTournamentMemState tournament,
            DbUpdater batch, Instant now) {
        sequentialExecutor.executeSync(placeCache.load(tournament.getPid()),
                place -> {
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    if (ScheduleService.TERMINAL_STATE.contains(tournament.getState())) {
                        tableService.bindPlace(place, batch, Optional.empty());
                    }
                    batch.onFailure(() -> placeCache.invalidate(tournament.getPid()));
                    return tableService.scheduleFreeTables(tournament, place, now, batch);
                });
    }

    public <T> T withPlace(Pid pid, Function<PlaceMemState, T> f) {
        return sequentialExecutor.executeSync(placeCache.load(pid), f);
    }
}
