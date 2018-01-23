package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.tournament.TournamentCacheFactory.TOURNAMENT_CACHE;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

public class TournamentCache implements Cache<Tid, TournamentMemState> {
    @Inject
    @Named(TOURNAMENT_CACHE)
    private LoadingCache<Tid, TournamentMemState> tournamentCache;

    @SneakyThrows
    public TournamentMemState load(Tid tid) {
        try {
            return tournamentCache.get(tid);
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    public void invalidate(int tid) {
        invalidate(new Tid(tid));
    }

    public void invalidate(Tid tid) {
        tournamentCache.invalidate(tid);
    }
}
