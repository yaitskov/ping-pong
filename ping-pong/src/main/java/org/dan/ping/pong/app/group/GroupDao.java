package org.dan.ping.pong.app.group;

import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.GROUPS;
import static org.dan.ping.pong.app.castinglots.GroupState.Open;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class GroupDao {
    @Inject
    private DSLContext jooq;

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<GroupInfo> getById(int gid) {
        return ofNullable(jooq.select(GROUPS.SORT, GROUPS.TID, GROUPS.CID)
                .from(GROUPS)
                .where(GROUPS.GID.eq(gid)).fetchOne())
                .map(r -> GroupInfo.builder()
                        .gid(gid)
                        .tid(r.get(GROUPS.TID))
                        .cid(r.get(GROUPS.CID))
                        .ordNumber(r.get(GROUPS.SORT))
                        .build());
    }

    @Transactional(TRANSACTION_MANAGER)
    public int createGroup(int tid, Integer cid, String label,
            int quits, int ordNumber) {
        final int gid = jooq.insertInto(GROUPS, GROUPS.TID, GROUPS.LABEL,
                GROUPS.STATE, GROUPS.CID, GROUPS.QUITS, GROUPS.SORT)
                .values(tid, label, Open, cid, quits, ordNumber)
                .returning(GROUPS.GID)
                .fetchOne()
                .getGid();
        log.info("Create group for category {} in tournament {}", cid, tid);
        return gid;
    }
}
