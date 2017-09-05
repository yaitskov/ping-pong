package org.dan.ping.pong.sys.seqex;

import java.time.Duration;
import java.util.function.Supplier;

public interface SequentialExecutor {
    void execute(Object id, Runnable r);
    <T> T executeSync(Object id, Duration timeout, Supplier <T> s);
}
