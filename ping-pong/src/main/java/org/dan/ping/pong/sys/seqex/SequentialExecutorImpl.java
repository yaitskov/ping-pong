package org.dan.ping.pong.sys.seqex;

import com.google.common.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


@RequiredArgsConstructor
public class SequentialExecutorImpl implements SequentialExecutor {
    private final LoadingCache<Object, Task> threadCache;

    @SneakyThrows
    public void execute(Object id, Runnable r) {
        Task task = threadCache.get(id);
        task.getInTask().put(r);
    }
}
