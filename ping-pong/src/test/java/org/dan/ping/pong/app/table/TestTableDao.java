package org.dan.ping.pong.app.table;

import static ord.dan.ping.pong.jooq.Tables.TABLES;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

public class TestTableDao {
    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public int delete(int pid) {
        return jooq.deleteFrom(TABLES)
                .where(TABLES.PID.eq(pid))
                .execute();
    }

    @Transactional(TRANSACTION_MANAGER)
    public List<Integer> tables(int pid) {
        return jooq.select(TABLES.TABLE_ID).from(TABLES)
                .where(TABLES.PID.eq(pid))
                .fetch()
                .map(r -> r.get(TABLES.TABLE_ID));
    }
}
