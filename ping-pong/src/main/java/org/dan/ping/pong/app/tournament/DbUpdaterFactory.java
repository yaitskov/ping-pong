package org.dan.ping.pong.app.tournament;

import org.dan.ping.pong.sys.db.DbUpdaterSql;
import org.jooq.DSLContext;

import javax.inject.Inject;

public class DbUpdaterFactory {
    @Inject
    private DSLContext jooq;

    public DbUpdaterSql create() {
        return DbUpdaterSql.create(jooq);
    }
}
