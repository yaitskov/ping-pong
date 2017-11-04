package org.dan.ping.pong.app.tournament;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class TournamentCacheFactory {
    public static final String TOURNAMENT_CACHE = "tournament-cache";

    @Inject
    private TournamentCacheLoader loader;

    @Value("${expire.tournament.seconds}")
    private int expireTournamentSeconds;

    @Bean(name = TOURNAMENT_CACHE)
    public LoadingCache<Tid, TournamentMemState> create() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(expireTournamentSeconds, TimeUnit.SECONDS)
                .build(loader);
    }
}
