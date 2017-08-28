package org.dan.ping.pong.sys.seqex;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@Slf4j
public class SeqexCtx {
    @Bean
    public SequentialExecutor sequentialExecutor(
            LoadingCache<Object, Task> cache) {
        return new SequentialExecutorImpl(cache);
    }

    @Bean
    protected LoadingCache<Object, Task> loadingCache(
            @Value("${executor.expire.minutes}") int expireAfter,
            @Value("${executor.queue.size}") int queueSize) {
        return CacheBuilder.<Object, Task>newBuilder()
                .expireAfterAccess(expireAfter, TimeUnit.MINUTES)
                .removalListener(new ActorRemovalListener())
                .build(new TaskFactory(queueSize));
    }
}
