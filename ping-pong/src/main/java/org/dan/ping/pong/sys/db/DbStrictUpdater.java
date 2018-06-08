package org.dan.ping.pong.sys.db;

import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.eclipse.jetty.http.HttpStatus.LOCKED_423;

import org.dan.ping.pong.sys.error.Error;
import org.dan.ping.pong.sys.error.PiPoEx;

public class DbStrictUpdater implements DbUpdater {
    public static final DbStrictUpdater DB_STRICT_UPDATER = new DbStrictUpdater();

    @Override
    public void flush() {
        // relax
    }

    @Override
    public void rollback() {
        throw new PiPoEx(LOCKED_423, new Error("Rollback transaction"), null);
    }

    @Override
    public DbUpdater onFailure(Runnable r) {
        throw internalError("not implemented");
    }

    @Override
    public DbUpdater exec(DbUpdate u) {
        ((DbUpdateSql) u).getQuery().execute();
        return this;
    }

    @Override
    public void markDirty() {
        // relax
    }
}
