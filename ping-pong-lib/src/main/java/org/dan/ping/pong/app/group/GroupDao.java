package org.dan.ping.pong.app.group;

import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Map;

public interface GroupDao {
    int createGroup(Tid tid, Integer cid, String label,
            int quits, int ordNumber);

    Map<Integer, GroupInfo> load(Tid tid);

    void deleteAllByTid(Tid tid, DbUpdater batch, int size);
}
