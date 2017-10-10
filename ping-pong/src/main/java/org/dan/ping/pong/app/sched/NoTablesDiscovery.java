package org.dan.ping.pong.app.sched;

import org.dan.ping.pong.app.table.TableInfo;

public class NoTablesDiscovery implements TablesDiscovery {
    public static final TableInfo STUB_TABLE = TableInfo.builder()
            .label(" - ")
            .build();

    public static final NoTablesDiscovery NO_TABLES_DISCOVERY = new NoTablesDiscovery();

    @Override
    public TableInfo discover(int tableId) {
        return STUB_TABLE;
    }
}
