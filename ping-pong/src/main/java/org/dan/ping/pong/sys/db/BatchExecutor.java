package org.dan.ping.pong.sys.db;

import java.util.List;

public interface BatchExecutor {
    List<Integer> execute(List<DbUpdateSql> updates);
}
