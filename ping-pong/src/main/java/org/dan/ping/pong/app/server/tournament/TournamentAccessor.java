package org.dan.ping.pong.app.server.tournament;

import org.dan.ping.pong.sys.seqex.SequentialExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Inject;

public class TournamentAccessor extends SeqAccessor<Tid, OpenTournamentMemState> {
    @Inject
    public TournamentAccessor(
            TournamentCache cache,
            DbUpdaterFactory dbUpdaterFactory,
            SequentialExecutor sequentialExecutor,
            PlatformTransactionManager tx) {
        super(cache, dbUpdaterFactory, sequentialExecutor, tx);
    }
}
