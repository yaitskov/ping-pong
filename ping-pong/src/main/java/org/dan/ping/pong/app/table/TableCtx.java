package org.dan.ping.pong.app.table;

import org.springframework.context.annotation.Import;

@Import({TableDaoServer.class, TableService.class, TableResource.class})
public class TableCtx {
}
