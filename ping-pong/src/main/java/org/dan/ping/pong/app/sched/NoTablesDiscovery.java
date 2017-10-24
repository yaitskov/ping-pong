package org.dan.ping.pong.app.sched;

import org.dan.ping.pong.app.table.TableInfo;

import java.util.Optional;

public class NoTablesDiscovery implements TablesDiscovery {
    public static final TableInfo STUB_TABLE = TableInfo.builder()
            .label(" - ")
            .build();

    public static final NoTablesDiscovery NO_TABLES_DISCOVERY = new NoTablesDiscovery();

    @Override
    public Optional<TableInfo> discover(int tableId) {
        return Optional.empty();
    }
}
