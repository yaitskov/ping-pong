package org.dan.ping.pong.app.sched;

import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.place.PlaceMemState;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.Set;
import java.util.function.Function;

public interface ScheduleService {
    void beginTournament(OpenTournamentMemState tournament,
            DbUpdater batch, Instant now);

    void cancelTournament(OpenTournamentMemState tournament,
            DbUpdater batch, Set<Integer> mids);

    void participantLeave(OpenTournamentMemState tournament,
            DbUpdater batch, Instant now);

    void afterMatchComplete(OpenTournamentMemState tournament,
            DbUpdater batch, Instant now);

    <T> T withPlace(Pid pid, Function<PlaceMemState, T> f);
}
