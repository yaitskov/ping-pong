package org.dan.ping.pong.app.sched;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.place.ArenaDistributionPolicy.NO;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.place.ArenaDistributionPolicy;
import org.dan.ping.pong.app.place.PlaceRules;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentTerminator;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class ScheduleServiceSelector implements ScheduleService {
    private final Map<ArenaDistributionPolicy, ScheduleService> schedulers;
    private final TournamentTerminator tournamentTerminator;

    @Override
    public void beginTournament(TournamentMemState tournament, DbUpdater batch, Instant now) {
        if (tournament.getMatches().isEmpty() && tournament.getState() == Open) {
            tournament.getCategories()
                    .keySet()
                    .forEach(cid -> tournamentTerminator.endOfTournamentCategory(
                            tournament, cid, batch));
            return;
        }
        dispatch(tournament).beginTournament(tournament, batch, now);
        tournament.getCondActions().runSchedule(tournament.getTid());
    }

    @Override
    public void cancelTournament(TournamentMemState tournament, DbUpdater batch, Instant now) {
        dispatch(tournament).cancelTournament(tournament, batch, now);
        tournament.getCondActions().runSchedule(tournament.getTid());
    }

    @Override
    public void participantLeave(TournamentMemState tournament, DbUpdater batch, Instant now) {
        dispatch(tournament).participantLeave(tournament, batch, now);
        tournament.getCondActions().runSchedule(tournament.getTid());
    }

    @Override
    public void afterMatchComplete(TournamentMemState tournament, DbUpdater batch, Instant now) {
        dispatch(tournament).afterMatchComplete(tournament, batch, now);
        tournament.getCondActions().runSchedule(tournament.getTid());
    }

    @Override
    public <T> T withPlaceTables(TournamentMemState tournament, Function<TablesDiscovery, T> f) {
        return dispatch(tournament).withPlaceTables(tournament, f);
    }

    private ScheduleService dispatch(TournamentMemState tournament) {
        final ArenaDistributionPolicy schedulerName = tournament.getRule()
                .getPlace().map(PlaceRules::getArenaDistribution).orElse(NO);
        return ofNullable(schedulers.get(schedulerName))
                .orElseThrow(() -> internalError("no scheduler " + schedulerName));
    }
}
