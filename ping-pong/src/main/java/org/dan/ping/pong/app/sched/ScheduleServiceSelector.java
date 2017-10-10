package org.dan.ping.pong.app.sched;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.place.ArenaDistributionPolicy.*;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.place.ArenaDistributionPolicy;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.place.PlaceMemState;
import org.dan.ping.pong.app.place.PlaceRules;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@RequiredArgsConstructor
public class ScheduleServiceSelector implements ScheduleService {
    private final Map<ArenaDistributionPolicy, ScheduleService> schedulers;

    @Override
    public void beginTournament(OpenTournamentMemState tournament, DbUpdater batch, Instant now) {
        dispatch(tournament).beginTournament(tournament, batch, now);
    }

    @Override
    public void cancelTournament(OpenTournamentMemState tournament, DbUpdater batch, Set<Integer> mids) {
        dispatch(tournament).cancelTournament(tournament, batch, mids);
    }

    @Override
    public void participantLeave(OpenTournamentMemState tournament, DbUpdater batch, Instant now) {
        dispatch(tournament).participantLeave(tournament, batch, now);
    }

    @Override
    public void afterMatchComplete(OpenTournamentMemState tournament, DbUpdater batch, Instant now) {
        dispatch(tournament).afterMatchComplete(tournament, batch, now);
    }

    @Override
    public <T> T withPlace(OpenTournamentMemState tournament, Function<TablesDiscovery, T> f) {
        return dispatch(tournament).withPlace(tournament, f);
    }

    private ScheduleService dispatch(OpenTournamentMemState tournament) {
        final ArenaDistributionPolicy schedulerName = tournament.getRule()
                .getPlace().map(PlaceRules::getArenaDistribution).orElse(NO);
        return ofNullable(schedulers.get(schedulerName))
                .orElseThrow(() -> internalError("no scheduler " + schedulerName));
    }
}
