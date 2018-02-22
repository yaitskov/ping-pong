package org.dan.ping.pong.app.sched;

import static com.google.common.collect.ImmutableMap.of;
import static org.dan.ping.pong.app.place.ArenaDistributionPolicy.GLOBAL;
import static org.dan.ping.pong.app.place.ArenaDistributionPolicy.NO;

import org.dan.ping.pong.app.tournament.TournamentTerminator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({GlobalScheduleService.class, NoScheduleService.class})
public class ScheduleCtx {
    public static final String SCHEDULE_SELECTOR = "schedule-selector";

    @Bean(name = SCHEDULE_SELECTOR)
    public ScheduleServiceSelector scheduleServiceSelector(
            GlobalScheduleService global,
            NoScheduleService noScheduleService,
            TournamentTerminator tournamentTerminator) {
        return new ScheduleServiceSelector(of(
                GLOBAL, global,
                NO, noScheduleService),
                tournamentTerminator);
    }
}
