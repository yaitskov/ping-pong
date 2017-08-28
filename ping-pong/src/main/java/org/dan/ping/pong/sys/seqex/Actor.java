package org.dan.ping.pong.sys.seqex;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

@Slf4j
@RequiredArgsConstructor
public class Actor {
    private final Object id;
    private final BlockingQueue<Runnable> inQueue;

    public void run() {
        while (true) {
            try {
                final Runnable r = inQueue.take();
                r.run();
            } catch (TerminateThread e) {
                log.info("Actor [{}] quits", id);
                break;
            } catch (InterruptedException e) {
                log.info("Actor [{}] drops interruption", id, e);
            }
        }
    }
}
