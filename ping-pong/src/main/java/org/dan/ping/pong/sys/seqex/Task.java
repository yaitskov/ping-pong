package org.dan.ping.pong.sys.seqex;

import lombok.Builder;
import lombok.Getter;

import java.util.concurrent.BlockingQueue;

@Getter
@Builder
public class Task {
    private final Object id;
    private Thread actor;
    private final BlockingQueue<Runnable> inTask;
}
