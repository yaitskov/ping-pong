package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

public class TournamentCacheFactory {
    @Inject
    private TournamentCacheLoader loader;

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> tournamentRelations;

    @Value("${expire.tournament.seconds}")
    private int expireTournamentSeconds;

    @Bean(name = TournamentCache.TOURNAMENT_CACHE)
    public LoadingCache<Tid, TournamentMemState> create() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(expireTournamentSeconds, TimeUnit.SECONDS)
                .removalListener((notification -> tournamentRelations.invalidate(notification.getKey())))
                .build(loader);
    }
}
