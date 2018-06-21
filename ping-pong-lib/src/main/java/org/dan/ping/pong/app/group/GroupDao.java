package org.dan.ping.pong.app.group;

import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.collection.MaxValue;

import java.util.List;
import java.util.Map;

public interface GroupDao {
    void createGroup(int gid, DbUpdater batch, Tid tid, Integer cid, String label,
            int quits);

    Map<Integer, GroupInfo> load(Tid tid, MaxValue<Integer> maxGid);

    void deleteAllByTid(Tid tid, DbUpdater batch, int size);

    void deleteByIds(DbUpdater batch, Tid tid, List<Integer> gids);
}
