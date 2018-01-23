package org.dan.ping.pong.app.tournament.console;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;

public class TournamentRelationCacheFactory {
    public static final String TOURNAMENT_RELATION_CACHE = "tournament-relation-cache";

    @Inject
    private TournamentRelationCacheLoader loader;

    @Bean(name = TOURNAMENT_RELATION_CACHE)
    public LoadingCache<Tid, RelatedTids> create() {
        return CacheBuilder.newBuilder().maximumSize(100).build(loader);
    }
}
