package org.dan.ping.pong.app.place;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class PlaceCacheFactory {
    @Inject
    private PlaceCacheLoader loader;

    @Value("${expire.place.seconds}")
    private int expirePlaceSeconds;

    @Bean(name = PlaceMemState.PLACE_CACHE)
    public LoadingCache<Pid, PlaceMemState> create() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(expirePlaceSeconds, TimeUnit.SECONDS)
                .build(loader);
    }
}
