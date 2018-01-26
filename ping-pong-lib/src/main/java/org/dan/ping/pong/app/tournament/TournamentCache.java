package org.dan.ping.pong.app.tournament;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

public class TournamentCache implements Cache<Tid, TournamentMemState> {
    public static final String TOURNAMENT_CACHE = "tournament-cache";
    public static final String TOURNAMENT_RELATION_CACHE = "tournament-relation-cache";

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
