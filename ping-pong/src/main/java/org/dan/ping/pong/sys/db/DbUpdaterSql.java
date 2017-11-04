package org.dan.ping.pong.sys.db;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.primitives.Ints.asList;
import static java.util.stream.Collectors.toList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Query;

import java.util.List;

@Slf4j
@Getter
@RequiredArgsConstructor
public class DbUpdaterSql implements DbUpdater {
    private final DSLContext jooq;
    private final List<DbUpdateSql> updates;
    private final List<Runnable> failureCallbacks;
    private boolean dirty;

    public static DbUpdaterSql create(DSLContext jooq) {
        return new DbUpdaterSql(jooq, newArrayList(), newArrayList());
    }

    public DbUpdater onFailure(Runnable r) {
        failureCallbacks.add(r);
        return this;
    }

    public DbUpdater exec(DbUpdate u) {
        return exec((DbUpdateSql) u);
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    public DbUpdater exec(DbUpdateSql u) {
        updates.add(u);
        return this;
    }

    @Override
    public void flush() {
        final List<Query> queries = updates.stream()
                .map(u -> {
                    u.getLogBefore().run();
                    return u.getQuery(); })
                .collect(toList());
        final List<Integer> updateRows = asList(jooq.batch(queries).execute());
        checkArgument(updateRows.size() == queries.size());
        for (int i = 0; i < updateRows.size(); ++i) {
            final int rowsUpdates = updateRows.get(i);
            final DbUpdateSql update = updates.get(i);
            update.getMustAffectRows()
                    .ifPresent(rowsExpected
                            -> checkAffectedRows(rowsExpected, rowsUpdates, update));
        }
        clear();
    }

    @SneakyThrows
    private void checkAffectedRows(int expectedRows, int affectedRows, DbUpdateSql update) {
        if (expectedRows >= 0 && affectedRows != expectedRows) {
            log.error("Batch query {} failed due expected rows {} but affected {}",
                    update.getQuery().getSQL(), expectedRows, affectedRows);
        } else if (expectedRows < 0 && affectedRows < -expectedRows) {
            log.error("Batch query {} failed due expected at least {} rows but affected {}",
                    update.getQuery().getSQL(), -expectedRows, affectedRows);
        } else {
            return;
        }
        update.getLogBefore().run();
        throw update.getOnFailure().apply(update);
    }

    private void clear() {
        dirty = false;
        updates.clear();
        failureCallbacks.clear();
    }

    @Override
    public void rollback() {
        if (dirty || !updates.isEmpty()) {
            failureCallbacks.forEach(r -> {
                try {
                    r.run();
                } catch (Exception e) {
                    log.error("Failure handler {}", r, e);
                    // restart node?
                }
            });
        }
        clear();
    }
}
