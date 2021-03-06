package org.dan.ping.pong.app.sched;

import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.table.TableInfo;

import java.util.Optional;

public interface TablesDiscovery {
    Optional<TableInfo> discover(Mid mid);
}
