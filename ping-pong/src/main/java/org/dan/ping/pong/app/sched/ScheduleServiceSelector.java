package org.dan.ping.pong.app.sched;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.category.CategoryState.Ply;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.place.ArenaDistributionPolicy.NO;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.category.CategoryMemState;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.place.ArenaDistributionPolicy;
import org.dan.ping.pong.app.place.PlaceRules;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentTerminator;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

public class ScheduleServiceSelector implements ScheduleService {
    @Inject
    private TournamentTerminator tournamentTerminator;
    @Inject
    private GlobalScheduleService global;
    @Inject
    private NoScheduleService noScheduleService;
    @Inject
    private MatchService matchService;

    private void completeGroupsWithoutMatches(
            TournamentMemState tournament, DbUpdater batch) {
        final Map<Gid, Long> gid2Matches = tournament.getMatches()
                .values().stream()
                .filter(m -> m.getState() != Over && m.getGid().isPresent())
                .map(MatchInfo::groupId)
                .collect(groupingBy(o -> o, counting()));
        tournament.getGroups().forEach((gid, grp) -> {
             final long notCompleteMatches = gid2Matches.getOrDefault(gid, 0L);
             if (notCompleteMatches == 0) {
                 matchService.completeGroupOf1(gid, tournament, batch);
             }
        });
    }

    private void completeCategoriesWithoutMatches(
            TournamentMemState tournament, DbUpdater batch) {
        final Map<Cid, CategoryMemState> playingCats = tournament.getCategories()
                .values().stream()
                .filter(c -> c.getState() == Ply)
                .collect(toMap(CategoryMemState::getCid, c -> c));

        final Map<Cid, Long> cid2Matches = tournament.getMatches()
                .values().stream()
                .filter(m -> m.getState() != Over)
                .collect(groupingBy(MatchInfo::getCid, counting()));

        playingCats.forEach((cid, cat) -> {
            final long notCompleteMatches = cid2Matches.getOrDefault(cid, 0L);
            if (notCompleteMatches == 0) {
                tournamentTerminator.endOfTournamentCategory(tournament, cid, batch);
            }
        });
    }

    @Override
    public void beginTournament(
            TournamentMemState tournament, DbUpdater batch, Instant now) {
        completeGroupsWithoutMatches(tournament, batch);
        completeCategoriesWithoutMatches(tournament, batch);
        dispatch(tournament).beginTournament(tournament, batch, now);
        tournament.getCondActions().runSchedule(tournament.getTid());
    }

    @Override
    public void cancelTournament(
            TournamentMemState tournament, DbUpdater batch, Instant now) {
        dispatch(tournament).cancelTournament(tournament, batch, now);
        tournament.getCondActions().runSchedule(tournament.getTid());
    }

    @Override
    public void participantLeave(
            TournamentMemState tournament, DbUpdater batch, Instant now) {
        dispatch(tournament).participantLeave(tournament, batch, now);
        tournament.getCondActions().runSchedule(tournament.getTid());
    }

    @Override
    public void afterMatchComplete(
            TournamentMemState tournament, DbUpdater batch, Instant now) {
        dispatch(tournament).afterMatchComplete(tournament, batch, now);
        tournament.getCondActions().runSchedule(tournament.getTid());
    }

    @Override
    public <T> T withPlaceTables(
            TournamentMemState tournament, Function<TablesDiscovery, T> f) {
        return dispatch(tournament).withPlaceTables(tournament, f);
    }

    private ScheduleService dispatch(TournamentMemState tournament) {
        final ArenaDistributionPolicy schedulerName = tournament.getRule()
                .getPlace().map(PlaceRules::getArenaDistribution).orElse(NO);
        switch (schedulerName) {
            case NO:
                return noScheduleService;
            case GLOBAL:
                return global;
            default:
                throw internalError("no scheduler " + schedulerName);
        }
    }
}
