package org.dan.ping.pong.app.group;

import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.collection.MaxValue;

import java.util.List;
import java.util.Map;

public interface GroupDao {
    void createGroup(Gid gid, DbUpdater batch, Tid tid, Cid cid, String label,
            int quits, int ordNumber);

    Map<Gid, GroupInfo> load(Tid tid, MaxValue<Gid> maxGid);

    void deleteAllByTid(Tid tid, DbUpdater batch, int size);

    void deleteByIds(DbUpdater batch, Tid tid, List<Gid> gids);
}
