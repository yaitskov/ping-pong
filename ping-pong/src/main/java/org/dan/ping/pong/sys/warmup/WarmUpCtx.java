package org.dan.ping.pong.sys.warmup;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;

@Import({WarmUpDao.class, WarmUpService.class, WarmUpResource.class,
        CleanUpWarmUpJob.CleanUpWarmUpJobDetail.class})
public class WarmUpCtx {
    public static final String WARM_UP_CACHE = "warm-up-cache";

    @Bean(name = WARM_UP_CACHE)
    public Cache<String, String> warmUpCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build();
    }
}
