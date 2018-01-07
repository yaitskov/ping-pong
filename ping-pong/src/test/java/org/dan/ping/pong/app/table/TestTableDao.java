package org.dan.ping.pong.app.table;

import static org.dan.ping.pong.jooq.Tables.TABLES;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.dan.ping.pong.app.place.Pid;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

public class TestTableDao {
    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public int delete(Pid pid) {
        return jooq.deleteFrom(TABLES)
                .where(TABLES.PID.eq(pid))
                .execute();
    }
}
