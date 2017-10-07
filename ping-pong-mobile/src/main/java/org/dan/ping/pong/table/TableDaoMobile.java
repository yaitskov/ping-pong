package org.dan.ping.pong.table;

import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.table.SetTableState;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.table.TableInfo;
import org.dan.ping.pong.app.table.TableStatedLink;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;
import java.util.Map;

import def.dom.
import def.dom4.
public class TableDaoMobile implements TableDao {
    @Override
    public void createTables(int pid, int numberOfNewTables) {

    }

    @Override
    public void locateMatch(TableInfo tableInfo, int mid, DbUpdater batch) {

    }

    @Override
    public void freeTable(int tableId, DbUpdater batch) {

    }

    @Override
    public List<TableStatedLink> findByPlaceId(int placeId) {
        return null;
    }

    @Override
    public void setStatus(SetTableState update, DbUpdater batch) {

    }

    @Override
    public Map<Integer, TableInfo> load(Pid pid) {
        return null;
    }
}
