package org.dan.ping.pong.sys.seqex;

public interface SequentialExecutor {
    void execute(Object id, Runnable r);
}
