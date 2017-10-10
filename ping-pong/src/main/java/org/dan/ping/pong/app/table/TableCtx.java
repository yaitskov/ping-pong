package org.dan.ping.pong.app.table;

import org.dan.ping.pong.app.sched.ScheduleService;
import org.springframework.context.annotation.Import;

@Import({TableDaoServer.class, TableService.class,
        TableResource.class, ScheduleService.class})
public class TableCtx {
}
