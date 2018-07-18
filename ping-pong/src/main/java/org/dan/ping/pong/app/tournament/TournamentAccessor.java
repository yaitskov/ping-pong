package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Inject;
import javax.inject.Named;

public class TournamentAccessor extends SeqAccessor<Tid, TournamentMemState> {
    private final LoadingCache<Tid, RelatedTids> tournamentRelation;

    @Inject
    public TournamentAccessor(
            @Named(TOURNAMENT_RELATION_CACHE)
                    LoadingCache<Tid, RelatedTids> tournamentRelation,
            TournamentCache cache,
            DbUpdaterFactory dbUpdaterFactory,
            SequentialExecutor sequentialExecutor,
            PlatformTransactionManager tx) {
        super(cache, dbUpdaterFactory, sequentialExecutor, tx);
        this.tournamentRelation = tournamentRelation;
    }

    @Override
    @SneakyThrows
    protected Tid mapSyncKey(Tid key) {
        return tournamentRelation.get(key).parentTid().orElse(key);
    }
}
