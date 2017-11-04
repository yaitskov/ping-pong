package org.dan.ping.pong.sys.db;

public interface DbUpdater {
    void flush();

    void rollback();

    DbUpdater onFailure(Runnable r);

    DbUpdater exec(DbUpdate u);

    void markDirty();
}
