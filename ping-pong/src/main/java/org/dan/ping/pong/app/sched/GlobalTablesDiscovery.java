package org.dan.ping.pong.app.sched;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.place.PlaceMemState;
import org.dan.ping.pong.app.table.TableInfo;

@RequiredArgsConstructor
public class GlobalTablesDiscovery implements TablesDiscovery {
    private final PlaceMemState place;

    @Override
    public TableInfo discover(int mid) {
        return place.getTableByMid(mid);
    }
}
