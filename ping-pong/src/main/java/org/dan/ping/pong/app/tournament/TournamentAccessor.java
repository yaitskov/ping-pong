package org.dan.ping.pong.app.tournament;

import org.dan.ping.pong.sys.seqex.SequentialExecutor;

import javax.inject.Inject;

public class TournamentAccessor extends SeqAccessor<Tid, OpenTournamentMemState> {
    @Inject
    public TournamentAccessor(
            TournamentCache cache,
            DbUpdaterFactory dbUpdaterFactory,
            SequentialExecutor sequentialExecutor) {
        super(cache, dbUpdaterFactory, sequentialExecutor);
    }
}
