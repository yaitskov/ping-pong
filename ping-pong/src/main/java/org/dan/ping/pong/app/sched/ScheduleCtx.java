package org.dan.ping.pong.app.sched;

import static com.google.common.collect.ImmutableMap.of;
import static org.dan.ping.pong.app.place.ArenaDistributionPolicy.GLOBAL;
import static org.dan.ping.pong.app.place.ArenaDistributionPolicy.NO;

import org.springframework.context.annotation.Bean;

public class ScheduleCtx {
    public static final String SCHEDULE_SELECTOR = "schedule-selector";

    @Bean(name = SCHEDULE_SELECTOR)
    public ScheduleServiceSelector scheduleServiceSelector(
            GlobalScheduleService global,
            NoScheduleService noScheduleService) {
        return new ScheduleServiceSelector(of(
                GLOBAL, global,
                NO, noScheduleService));
    }

    @Bean
    protected GlobalScheduleService globalScheduleService() {
        return new GlobalScheduleService();
    }

    @Bean
    protected NoScheduleService noScheduleService() {
        return new NoScheduleService();
    }
}
