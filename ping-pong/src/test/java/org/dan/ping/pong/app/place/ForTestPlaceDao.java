package org.dan.ping.pong.app.place;

import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT;
import static ord.dan.ping.pong.jooq.tables.PlaceAdmin.PLACE_ADMIN;
import static ord.dan.ping.pong.jooq.tables.Tables.TABLES;
import static org.dan.ping.pong.app.table.TableState.Free;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.dan.ping.pong.app.table.TableInfo;
import org.dan.ping.pong.app.table.TableState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.Uid;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

public class ForTestPlaceDao {
    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public void revokeAdmin(Pid pid, Uid uid) {
        jooq.deleteFrom(PLACE_ADMIN)
                .where(PLACE_ADMIN.PID.eq(pid), PLACE_ADMIN.UID.eq(uid))
                .execute();
    }

    public List<TableInfo> findFreeTables(Tid tid) {
        return findTournamentTablesByState(tid, Free);
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    private List<TableInfo> findTournamentTablesByState(Tid tid, TableState state) {
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
}
