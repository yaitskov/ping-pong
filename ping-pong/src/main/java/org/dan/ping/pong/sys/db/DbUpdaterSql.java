package org.dan.ping.pong.sys.db;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@RequiredArgsConstructor
public class DbUpdaterSql implements DbUpdater {
    private final BatchExecutor batchExecutor;
    private final List<DbUpdateSql> updates;
    private final List<Runnable> failureCallbacks;
    private boolean dirty;

    public static DbUpdaterSql create(BatchExecutor batchExecutor) {
        return new DbUpdaterSql(batchExecutor, newArrayList(), newArrayList());
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
        log.info("*** Flush db {} queries ***", updates.size());

        final List<Integer> updateRows = batchExecutor.execute(updates);
        checkArgument(updateRows.size() == updates.size(),
                "%s <> %s", updateRows.size(), updates.size());
        for (int i = 0; i < updateRows.size(); ++i) {
            final int rowsUpdates = updateRows.get(i);
            final DbUpdateSql update = updates.get(i);
            update.getMustAffectRows()
                    .ifPresent(rowsExpected
                            -> checkAffectedRows(rowsExpected, rowsUpdates, update));
        }
        clear();
        log.info("*** Flush db {} queries is complete ***", updates.size());
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
