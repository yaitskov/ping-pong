package org.dan.ping.pong.sys.seqex;

import com.google.common.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class SequentialExecutorImpl implements SequentialExecutor {
    private final LoadingCache<Object, Task> threadCache;

    @SneakyThrows
    public void execute(Object id, Runnable r) {
        final Task task = threadCache.get(id);
        task.getInTask().put(r);
    }

    @Override
    @SneakyThrows
    public <A, R> R executeSync(A o, Function<A, R> s) {
        synchronized (o) {
            return s.apply(o);
        }
    }
}
