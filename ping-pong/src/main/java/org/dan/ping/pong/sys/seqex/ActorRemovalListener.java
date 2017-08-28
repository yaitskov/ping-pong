package org.dan.ping.pong.sys.seqex;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActorRemovalListener implements RemovalListener<Object, Task> {
    public void onRemoval(RemovalNotification<Object, Task> notification) {
        final Task task = notification.getValue();
        final Thread.State state = task.getActor().getState();
        final int items = task.getInTask().size();
        if (state != Thread.State.WAITING || items != 0) {
            log.error("Evict acting actor [{}] state [{}] with {} elements",
                    notification.getKey(), state, items);
        }
        try {
            task.getInTask().put(() -> {
                throw new TerminateThread();
            });
        } catch (InterruptedException e) {
            log.error("Terminating thread {} was interrupted",
                    notification.getKey(), e);
        }
    }
}
