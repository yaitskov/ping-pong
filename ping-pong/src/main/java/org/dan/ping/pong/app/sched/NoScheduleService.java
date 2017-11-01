package org.dan.ping.pong.app.sched;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.sched.NoTablesDiscovery.NO_TABLES_DISCOVERY;

import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.table.TableService;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;

public class NoScheduleService implements ScheduleService {
    @Inject
    private TableService tableService;

    @Inject
    private BidDao bidDao;

    @Override
    public void beginTournament(OpenTournamentMemState tournament,
            DbUpdater batch, Instant now) {
        selectForScheduling(tournament).forEach(match -> {
            tableService.markAsSchedule(match, now, batch);
            bidDao.markParticipantsBusy(tournament, match.getUids(), now, batch);
        });
    }

    @Override
    public void cancelTournament(OpenTournamentMemState tournament,
            DbUpdater batch, Instant now) {
        beginTournament(tournament, batch, now);
    }

    @Override
    public void participantLeave(OpenTournamentMemState tournament,
            DbUpdater batch, Instant now) {
        beginTournament(tournament, batch, now);
    }

    @Override
    public void afterMatchComplete(OpenTournamentMemState tournament,
            DbUpdater batch, Instant now) {
        beginTournament(tournament, batch, now);
    }

    @Override
    public <T> T withPlaceTables(OpenTournamentMemState tournament, Function<TablesDiscovery, T> f) {
        return f.apply(NO_TABLES_DISCOVERY);
    }

    private List<MatchInfo> selectForScheduling(OpenTournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getState() == Place)
                .filter(minfo -> minfo.getParticipantIdScore().size() == 2)
                .sorted(comparingInt(MatchInfo::getPriority)
                        .thenComparingInt(MatchInfo::getMid))
                .collect(toList());
    }
}
