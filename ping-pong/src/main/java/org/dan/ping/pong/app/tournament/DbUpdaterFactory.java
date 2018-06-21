package org.dan.ping.pong.app.tournament;

import org.dan.ping.pong.sys.db.BatchExecutor;
import org.dan.ping.pong.sys.db.DbUpdaterSql;

import javax.inject.Inject;

public class DbUpdaterFactory {
    @Inject
    private BatchExecutor batchExecutor;

    public DbUpdaterSql create() {
        return DbUpdaterSql.create(batchExecutor);
    }
}
