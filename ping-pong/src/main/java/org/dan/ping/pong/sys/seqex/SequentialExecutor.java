package org.dan.ping.pong.sys.seqex;

import java.util.function.Function;

public interface SequentialExecutor {
    void execute(Object id, Runnable r);
    <A, R> R executeSync(A id, Function<A, R> s);
}
