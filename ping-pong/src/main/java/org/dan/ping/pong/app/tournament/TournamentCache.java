package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.tournament.TournamentCacheFactory.TOURNAMENT_CACHE;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

public class TournamentCache {
    @Inject
    @Named(TOURNAMENT_CACHE)
    private LoadingCache<Tid, OpenTournamentMemState> tournamentCache;

    @SneakyThrows
    public OpenTournamentMemState load(int tid) {
        try {
            return tournamentCache.get(new Tid(tid));
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    public void invalidate(int tid) {
        tournamentCache.invalidate(new Tid(tid));
    }
}
