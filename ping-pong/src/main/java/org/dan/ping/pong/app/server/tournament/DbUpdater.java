package org.dan.ping.pong.app.server.tournament;

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
public class DbUpdater {
    private final DSLContext jooq;
    private final List<DbUpdate> updates;
    private final List<Runnable> failureCallbacks;

    public static DbUpdater create(DSLContext jooq) {
        return new DbUpdater(jooq, newArrayList(), newArrayList());
    }

    public DbUpdater onFailure(Runnable r) {
        failureCallbacks.add(r);
        return this;
    }

    public DbUpdater exec(DbUpdate u) {
        updates.add(u);
        return this;
    }

    public DbUpdater exec(Query q) {
        return exec(DbUpdate.builder().query(q).build());
    }

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
            final DbUpdate update = updates.get(i);
            update.getMustAffectRows()
                    .ifPresent(rowsExpected
                            -> checkAffectedRows(rowsExpected, rowsUpdates, update));
        }
        clear();
    }

    @SneakyThrows
    private void checkAffectedRows(int expectedRows, int affectedRows, DbUpdate update) {
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
        updates.clear();
        failureCallbacks.clear();
    }

    public void rollback() {
        failureCallbacks.forEach(r -> {
            try {
                r.run();
            } catch (Exception e) {
                log.error("Failure handler {}", r, e);
                // restart node?
            }
        });
        clear();
    }
}
