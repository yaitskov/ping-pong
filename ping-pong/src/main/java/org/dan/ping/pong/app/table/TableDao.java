package org.dan.ping.pong.app.table;

import static com.google.common.primitives.Ints.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;
import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT;
import static ord.dan.ping.pong.jooq.tables.Tables.TABLES;
import static org.dan.ping.pong.sys.db.DbUpdateSql.JUST_A_ROW;
import static org.dan.ping.pong.sys.db.DbUpdateSql.NON_ZERO_ROWS;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.app.table.TableState.Busy;
import static org.dan.ping.pong.app.table.TableState.Free;

import lombok.extern.slf4j.Slf4j;
import ord.dan.ping.pong.jooq.Tables;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.sys.db.DbUpdateSql;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.sys.db.DbUpdaterSql;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
        final Record1<String> record = jooq.select(TABLES.LABEL)
                .from(TABLES)
                .where(TABLES.PID.eq(pid),
                        TABLES.LABEL.likeRegex("^[0-9]+$"))
                .orderBy(TABLES.LABEL.length().desc(), TABLES.LABEL.desc())
                .limit(1)
                .fetchOne();

        int highestTableLabel = 0;
        if (record != null) {
            highestTableLabel = Integer.parseInt(record.value1()) + 1;
        }
        log.info("Added {}", asList(jooq.batch(
                IntStream.range(highestTableLabel, numberOfNewTables + highestTableLabel)
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

    public void locateMatch(TableInfo tableInfo, int mid, DbUpdater batch) {
        tableInfo.setState(Busy);
        tableInfo.setMid(Optional.of(mid));
        batch.exec(DbUpdateSql.builder()
                .logBefore(() -> log.info("Mid {} will be hold on table {}",
                        mid, tableInfo.getTableId()))
                .onFailure(u -> internalError("Failed placing mid "
                        + mid + " on table " + tableInfo.getTableId()))
                .mustAffectRows(JUST_A_ROW)
                .query(jooq.update(TABLES)
                        .set(TABLES.STATE, Busy)
                        .set(TABLES.MID, of(mid))
                        .where(TABLES.TABLE_ID.eq(tableInfo.getTableId()),
                                TABLES.STATE.eq(Free)))
                .build());
    }

    @Transactional(TRANSACTION_MANAGER)
    public void freeTable(int tableId, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .mustAffectRows(NON_ZERO_ROWS)
                .logBefore(() -> log.info("Return table of table {}", tableId))
                .onFailure((u) -> internalError("Table " + tableId + " doesn't have a match"))
                .query(jooq.update(TABLES)
                        .set(TABLES.MID, empty())
                        .set(TABLES.STATE, Free)
                        .where(TABLES.TABLE_ID.eq(tableId),
                                TABLES.STATE.eq(Busy)))
                .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<TableStatedLink> findByPlaceId(int placeId) {
        return jooq.select(TABLES.TABLE_ID, TABLES.LABEL, TABLES.STATE)
                .from(TABLES)
                .where(Tables.TABLES.PID.eq(placeId))
                .fetch()
                .map(r -> TableStatedLink
                        .builder()
                        .label(r.get(TABLES.LABEL))
                        .id(r.get(TABLES.TABLE_ID))
                        .state(r.get(TABLES.STATE))
                        .build());
    }

    public void setStatus(SetTableState update, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .mustAffectRows(JUST_A_ROW)
                .onFailure(u -> badRequest("Table state changed"))
                .query(jooq.update(TABLES)
                        .set(TABLES.STATE, update.getTarget())
                        .where(TABLES.TABLE_ID.eq(update.getTableId()),
                                TABLES.STATE.eq(update.getExpected())))
                .build());
    }

    public Map<Integer, TableInfo> load(Pid pid) {
        return jooq.select().from(TABLES)
                .where(TABLES.PID.eq(pid.getPid()))
                .fetch()
                .stream()
                .collect(toMap(r -> r.get(TABLES.TABLE_ID),
                        r -> TableInfo.builder()
                                .mid(r.get(Tables.TABLES.MID))
                                .pid(pid.getPid())
                                .state(r.get(TABLES.STATE))
                                .label(r.get(TABLES.LABEL))
                                .tableId(r.get(TABLES.TABLE_ID))
                                .build()));
    }
}
