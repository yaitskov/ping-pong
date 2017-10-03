package org.dan.ping.pong.app.place;

import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class PlaceCacheFactory {
    public static final String PLACE_CACHE = "place-cache";

    @Inject
    private PlaceCacheLoader loader;

    @Value("${expire.place.seconds}")
    private int expirePlaceSeconds;

    @Bean(name = PLACE_CACHE)
    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public LoadingCache<Pid, PlaceMemState> create() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(expirePlaceSeconds, TimeUnit.SECONDS)
                .build(loader);
    }
}
