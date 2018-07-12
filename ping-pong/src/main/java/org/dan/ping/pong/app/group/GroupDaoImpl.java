package org.dan.ping.pong.app.group;

import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.jooq.Tables.GROUPS;
import static org.dan.ping.pong.app.castinglots.GroupState.Open;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.sys.db.DbUpdateSql;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.util.collection.MaxValue;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class GroupDaoImpl implements GroupDao {
    @Inject
    private DSLContext jooq;

    @Override
    public void createGroup(Gid gid, DbUpdater batch, Tid tid, Cid cid, String label,
            int quits, int ordNumber) {
        batch.exec(DbUpdateSql
                .builder()
                .query(jooq.insertInto(GROUPS, GROUPS.GID, GROUPS.TID, GROUPS.LABEL,
                        GROUPS.STATE, GROUPS.CID, GROUPS.QUITS, GROUPS.SORT)
                        .values(gid, tid, label, Open, cid, quits, ordNumber))
                .mustAffectRows(Optional.of(1))
                .onFailure((u) -> internalError("Failed to create a group"))
                .build());
    }

    @Override
    public Map<Gid, GroupInfo> load(Tid tid, MaxValue<Gid> maxGid) {
        return jooq.select(GROUPS.GID, GROUPS.CID, GROUPS.SORT, GROUPS.LABEL)
                .from(GROUPS)
                .where(GROUPS.TID.eq(tid))
                .fetch()
                .stream()
                .collect(toMap(r -> maxGid.apply(r.get(GROUPS.GID)),
                        r -> GroupInfo.builder()
                                .gid(r.get(GROUPS.GID))
                                .cid(r.get(GROUPS.CID))
                                .ordNumber(r.get(GROUPS.SORT))
                                .label(r.get(GROUPS.LABEL))
                                .build()));

    }

    @Override
    public void deleteAllByTid(Tid tid, DbUpdater batch, int size) {
        batch.exec(DbUpdateSql.builder()
                .mustAffectRows(Optional.of(size))
                .query(jooq.deleteFrom(GROUPS)
                        .where(GROUPS.TID.eq(tid))).build());
    }

    @Override
    public void deleteByIds(DbUpdater batch, Tid tid, List<Gid> gids) {
        batch.exec(DbUpdateSql.builder()
                .mustAffectRows(Optional.of(gids.size()))
                .query(jooq.deleteFrom(GROUPS)
                        .where(GROUPS.TID.eq(tid), GROUPS.GID.in(gids)))
                .build());
    }
}
