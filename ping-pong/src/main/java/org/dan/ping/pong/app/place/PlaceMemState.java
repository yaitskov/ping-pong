package org.dan.ping.pong.app.place;

import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.match.Pid;
import org.dan.ping.pong.app.table.TableInfo;

import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class PlaceMemState {
    private final Pid pid;
    private String name;
    private Map<Integer, TableInfo> tables;

    public TableInfo getTableByMid(int mid) {
        return tables.values().stream()
                .filter(tbl -> tbl.getMid().equals(Optional.of(mid)))
                .findAny()
                .orElseThrow(() -> internalError("Match "
                        + mid + " is not bound to any table in " + pid));
    }
}
