package org.dan.ping.pong.app.tournament.console;

import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;

public class TournamentRelationCacheFactory {
    @Inject
    private TournamentRelationCacheLoader loader;

    @Bean(name = TOURNAMENT_RELATION_CACHE)
    public LoadingCache<Tid, RelatedTids> create() {
        return CacheBuilder.newBuilder().maximumSize(100).build(loader);
    }
}
