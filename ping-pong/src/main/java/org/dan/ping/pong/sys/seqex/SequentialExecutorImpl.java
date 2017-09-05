package org.dan.ping.pong.sys.seqex;

import com.google.common.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
    public <T> T executeSync(Object id, Duration timeout, Supplier<T> s) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        execute(id, () -> {
            try {
                future.complete(s.get());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
}
