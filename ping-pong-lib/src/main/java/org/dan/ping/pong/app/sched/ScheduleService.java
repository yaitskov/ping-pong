package org.dan.ping.pong.app.sched;

import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.function.Function;

public interface ScheduleService {
    void beginTournament(TournamentMemState tournament,
            DbUpdater batch, Instant now);

    void cancelTournament(TournamentMemState tournament,
            DbUpdater batch, Instant now);

    void participantLeave(TournamentMemState tournament,
            DbUpdater batch, Instant now);

    void afterMatchComplete(TournamentMemState tournament,
            DbUpdater batch, Instant now);

    <T> T withPlaceTables(TournamentMemState tournament, Function<TablesDiscovery, T> f);
}
