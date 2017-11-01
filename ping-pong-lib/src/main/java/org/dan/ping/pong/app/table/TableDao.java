package org.dan.ping.pong.app.table;

import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;
import java.util.Map;

public interface TableDao {
    void createTables(Pid pid, int numberOfNewTables);

    void locateMatch(TableInfo tableInfo, int mid, DbUpdater batch);

    void freeTable(int tableId, DbUpdater batch);

    List<TableStatedLink> findByPlaceId(Pid placeId);

    void setStatus(SetTableState update, DbUpdater batch);

    Map<Integer, TableInfo> load(Pid pid);
}
