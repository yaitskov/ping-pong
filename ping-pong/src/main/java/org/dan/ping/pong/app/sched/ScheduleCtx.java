package org.dan.ping.pong.app.sched;

import org.springframework.context.annotation.Import;

@Import({GlobalScheduleService.class,
        NoScheduleService.class,
        ScheduleServiceSelector.class})
public class ScheduleCtx {}
