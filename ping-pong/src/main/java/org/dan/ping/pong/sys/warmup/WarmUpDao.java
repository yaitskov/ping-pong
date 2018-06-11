package org.dan.ping.pong.sys.warmup;

import static org.dan.ping.pong.jooq.tables.WarmUp.WARM_UP;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.dan.ping.pong.app.bid.Uid;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import javax.inject.Inject;

public class WarmUpDao {
    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public int warmUp(Uid uid, WarmUpRequest request, Instant now) {
        return jooq
                .insertInto(
                        WARM_UP, WARM_UP.BEFORE_ACTION, WARM_UP.UID,
                        WARM_UP.CREATED, WARM_UP.CLIENT_STARTED)
                .values(request.getAction(), uid, now, request.getClientTime())
                .execute();
    }

    @Transactional(TRANSACTION_MANAGER)
    public int logDuration(int warmUpId, Instant now) {
        return jooq.update(WARM_UP)
                .set(WARM_UP.COMPLETE_AT, now)
                .where(WARM_UP.WM_ID.eq(warmUpId))
                .execute();
    }

    @Transactional(TRANSACTION_MANAGER)
    public int cleanOlderThan(Instant cutPoint) {
        return jooq.deleteFrom(WARM_UP)
                .where(WARM_UP.CREATED.lessThan(cutPoint))
                .execute();
    }
}
