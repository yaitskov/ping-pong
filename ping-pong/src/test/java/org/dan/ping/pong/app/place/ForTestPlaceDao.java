package org.dan.ping.pong.app.place;

import static ord.dan.ping.pong.jooq.tables.PlaceAdmin.PLACE_ADMIN;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

public class ForTestPlaceDao {
    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public void revokeAdmin(int pid, int uid) {
        jooq.deleteFrom(PLACE_ADMIN)
                .where(PLACE_ADMIN.PID.eq(pid), PLACE_ADMIN.UID.eq(uid))
                .execute();
    }
}
