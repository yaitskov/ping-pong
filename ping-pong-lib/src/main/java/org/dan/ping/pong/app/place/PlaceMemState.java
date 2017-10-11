package org.dan.ping.pong.app.place;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.table.TableInfo;
import org.dan.ping.pong.app.tournament.Uid;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@Builder
public class PlaceMemState {
    public static final String PID = "pid";
    public static final String NO_ADMIN_ACCESS_TO_PLACE = "no-admin-access-to-place";

    private final Pid pid;
    private String name;
    private Map<Integer, TableInfo> tables;
    private Set<Uid> adminIds;
    private Optional<Integer> hostingTid;

    public TableInfo getTableByMid(int mid) {
        return tables.values().stream()
                .filter(tbl -> tbl.getMid().equals(Optional.of(mid)))
                .findAny()
                .orElseThrow(() -> internalError("Match "
                        + mid + " is not bound to any table in " + pid));
    }

    public TableInfo getTable(int tableId) {
        return ofNullable(tables.get(tableId))
                .orElseThrow(() -> notFound("Table " + tableId
                        + " is not in place " + pid));
    }

    public void checkAdmin(Uid uid) {
        if (adminIds.contains(uid)) {
            return;
        }
        throw forbidden(NO_ADMIN_ACCESS_TO_PLACE, PID, pid.getPid());
    }
}
