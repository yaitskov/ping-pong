package org.dan.ping.pong.app.sched;

import org.dan.ping.pong.app.table.TableInfo;

public interface TablesDiscovery {
    TableInfo discover(int tableId);
}
