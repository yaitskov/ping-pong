package org.dan.ping.pong.app.table;

import org.dan.ping.pong.app.sched.GlobalScheduleService;
import org.springframework.context.annotation.Import;

@Import({TableDaoServer.class, TableService.class,
        TableResource.class, GlobalScheduleService.class})
public class TableCtx {
}
