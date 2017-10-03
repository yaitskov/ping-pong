package org.dan.ping.pong.app.server.tournament;

import static org.dan.ping.pong.app.server.tournament.TournamentCacheFactory.TOURNAMENT_CACHE;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

public class TournamentCache implements Cache<Tid, OpenTournamentMemState> {
    @Inject
    @Named(TOURNAMENT_CACHE)
    private LoadingCache<Tid, OpenTournamentMemState> tournamentCache;

    public OpenTournamentMemState load(int tid) {
        return load(new Tid(tid));
    }

    @SneakyThrows
    public OpenTournamentMemState load(Tid tid) {
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
