package org.dan.ping.pong.app.group;

import static java.util.stream.Collectors.toMap;
import static ord.dan.ping.pong.jooq.Tables.GROUPS;
import static org.dan.ping.pong.app.castinglots.GroupState.Open;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.tournament.Tid;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import javax.inject.Inject;

@Slf4j
public class GroupDao {
    @Inject
    private DSLContext jooq;

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

    public Map<Integer, GroupInfo> load(Tid tid) {
        return jooq.select(GROUPS.GID, GROUPS.CID, GROUPS.SORT, GROUPS.LABEL)
                .from(GROUPS)
                .where(GROUPS.TID.eq(tid.getTid()))
                .fetch()
                .stream()
                .collect(toMap(r -> r.get(GROUPS.GID),
                        r -> GroupInfo.builder()
                                .gid(r.get(GROUPS.GID))
                                .cid(r.get(GROUPS.CID))
                                .ordNumber(r.get(GROUPS.SORT))
                                .label(r.get(GROUPS.LABEL))
                                .build()));

    }
}
