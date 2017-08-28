package org.dan.ping.pong.sys.seqex;

import com.google.common.cache.CacheLoader;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class TaskFactory extends CacheLoader<Object, Task> {
    private final int queueSize;

    public TaskFactory(int queueSize) {
        this.queueSize = queueSize;
    }

    @Override
    public Task load(Object key) throws Exception {
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueSize);
        final Thread executor = new Thread(new Actor(key, queue)::run, "exec-" + key);
        executor.start();
        return Task.builder().id(key).inTask(queue).actor(executor).build();
    }
}
