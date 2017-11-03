package org.dan.ping.pong.app.sched;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.place.PlaceMemState;
import org.dan.ping.pong.app.table.TableInfo;

import java.util.Optional;

@RequiredArgsConstructor
public class GlobalTablesDiscovery implements TablesDiscovery {
    private final PlaceMemState place;

    @Override
    public Optional<TableInfo> discover(Mid mid) {
        return place.findTableByMid(mid);
    }
}
