package org.dan.ping.pong.sys.warmup;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.jooq.tables.WarmUp.WARM_UP;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.util.OptPlus.oMap2;

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
                        WARM_UP.CREATED, WARM_UP.WARM_UP_STARTED)
                .values(request.getAction(), uid, now, request.getWarmUpStarted())
                .returning()
                .fetchOne()
                .get(WARM_UP.WM_ID);
    }

    @Transactional(TRANSACTION_MANAGER)
    public long readDuration(int warmUpId) {
        return ofNullable(
                jooq.select(WARM_UP.COMPLETE_AT, WARM_UP.CLIENT_STARTED)
                        .from(WARM_UP)
                        .where(WARM_UP.WM_ID.eq(warmUpId))
                        .fetchOne())
                .orElseThrow(() -> badRequest("wm id is not valid [" + warmUpId + "]"))
                .map(r ->
                        oMap2((a, b) -> b - a,
                                ofNullable(r.get(WARM_UP.CLIENT_STARTED))
                                        .map(Instant::toEpochMilli),
                                ofNullable(r.get(WARM_UP.COMPLETE_AT))
                                        .map(Instant::toEpochMilli)))
                .orElse(0L);
    }

    @Transactional(TRANSACTION_MANAGER)
    public int logDuration(int warmUpId, Instant now, Instant clientStarted) {
        return jooq.update(WARM_UP)
                .set(WARM_UP.COMPLETE_AT, now)
                .set(WARM_UP.CLIENT_STARTED, clientStarted)
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
