package org.dan.ping.pong.app.sched;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.bid.BidDao.TERMINAL_BID_STATES;
import static org.dan.ping.pong.app.place.ArenaDistributionPolicy.NO;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.app.tournament.TournamentState.TERMINAL_STATE;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.place.ArenaDistributionPolicy;
import org.dan.ping.pong.app.place.PlaceRules;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentTerminator;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

@RequiredArgsConstructor
public class ScheduleServiceSelector implements ScheduleService {
    private final Map<ArenaDistributionPolicy, ScheduleService> schedulers;
    private final TournamentTerminator tournamentTerminator;

    @Override
    public void beginTournament(TournamentMemState tournament, DbUpdater batch, Instant now) {
        if (!tournament.getGroups().isEmpty()) {
            completeGroupsWithOneParticipant(tournament, batch);
        }
        if (!tournament.getRule().getPlayOff().isPresent()) {
            completeLadderWithOneParticipant(tournament, batch);
        }
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

    private void completeLadderWithOneParticipant(TournamentMemState tournament, DbUpdater batch) {

    }

    @Inject
    private MatchService matchService;

    private void completeGroupsWithOneParticipant(
            TournamentMemState tournament, DbUpdater batch) {
        final Map<Gid, Long> group2LiveParticipants = tournament.getParticipants()
                .values().stream()
                .filter(p -> p.getGid().isPresent())
                .filter(p -> !TERMINAL_BID_STATES.contains(p.getBidState()))
                .collect(groupingBy(p -> p.getGid().get(), counting()));

        tournament.getGroups().values().forEach((gInfo) -> {
            if (group2LiveParticipants.getOrDefault(gInfo.getGid(), 0L) == 1L) {
                matchService.tryToCompleteGroup(tournament, gInfo.getGid(), batch);
            }
        });
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
