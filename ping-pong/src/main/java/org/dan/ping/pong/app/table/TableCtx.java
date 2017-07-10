package org.dan.ping.pong.app.table;

import org.springframework.context.annotation.Import;

@Import({TableDao.class, TableService.class, TableResource.class})
public class TableCtx {
}
