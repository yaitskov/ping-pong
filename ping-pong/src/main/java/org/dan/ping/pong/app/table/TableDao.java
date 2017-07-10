package org.dan.ping.pong.app.table;

import static com.google.common.primitives.Ints.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT;
import static ord.dan.ping.pong.jooq.tables.Tables.TABLES;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.app.table.TableState.Busy;
import static org.dan.ping.pong.app.table.TableState.Free;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

@Slf4j
public class TableDao {
    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public void createTables(int pid, int numberOfNewTables) {
        log.info("Add {} tables to place {}", numberOfNewTables, pid);
        final int tablesPresented = jooq.selectCount()
                .from(TABLES)
                .where(TABLES.PID.eq(pid))
                .fetchOne().value1();
        log.info("Added {}", asList(jooq.batch(
                IntStream.range(tablesPresented, numberOfNewTables + tablesPresented)
                        .mapToObj(String::valueOf)
                        .map(label -> jooq.insertInto(TABLES, TABLES.PID,
                                TABLES.STATE, TABLES.LABEL)
                                .values(pid, Free, label))
                        .collect(Collectors.toList()))
                .execute()));
    }

    public List<TableInfo> findFreeTables(int tid) {
        return findTournamentTablesByState(tid, Free);
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<TableInfo> findTournamentTablesByState(int tid, TableState state) {
        return jooq.select(TABLES.TABLE_ID, TABLES.LABEL, TABLES.PID, TABLES.MID)
                .from(TABLES)
                .innerJoin(TOURNAMENT)
                .on(TABLES.PID.eq(TOURNAMENT.PID))
                .where(TOURNAMENT.TID.eq(tid),
                        TABLES.STATE.eq(state))
                .fetch()
                .map(r -> TableInfo.builder()
                        .pid(r.get(TABLES.PID))
                        .tableId(r.get(TABLES.TABLE_ID))
                        .state(state)
                        .mid(r.get(TABLES.MID))
                        .label(r.get(TABLES.LABEL))
                        .build());
    }

    @Transactional(TRANSACTION_MANAGER)
    public void locateMatch(int tableId, int mid) {
        if (0 == jooq.update(TABLES)
                .set(TABLES.STATE, Busy)
                .set(TABLES.MID, of(mid))
                .where(TABLES.TABLE_ID.eq(tableId),
                        TABLES.STATE.eq(Free))
                .execute()) {
            throw internalError("Failed placing mid "
                    + mid + " on table " + tableId);
        }
    }

    @Transactional(TRANSACTION_MANAGER)
    public void freeTable(int mid) {
        log.info("Return table of mid {}", mid);
        if (0 == jooq.update(TABLES)
                .set(TABLES.MID, empty())
                .set(TABLES.STATE, Free)
                .where(TABLES.MID.eq(Optional.of(mid)),
                        TABLES.STATE.eq(Busy))
                .execute()) {
            throw internalError("Mid " + mid + " doesn't have a table");
        }
    }

    @Transactional(TRANSACTION_MANAGER)
    public boolean hasUsableTables(int tid) {
        return jooq.selectCount()
                .from(TABLES)
                .innerJoin(TOURNAMENT)
                .on(TABLES.PID.eq(TOURNAMENT.PID))
                .where(TOURNAMENT.TID.eq(tid),
                        TABLES.STATE.in(Busy, Free))
                .fetchOne()
                .value1() > 0;
    }
}
