package org.dan.ping.pong.app.tournament;

import org.jooq.DSLContext;

import javax.inject.Inject;

public class DbUpdaterFactory {
    @Inject
    private DSLContext jooq;

    public DbUpdater create() {
        return DbUpdater.create(jooq);
    }
}
