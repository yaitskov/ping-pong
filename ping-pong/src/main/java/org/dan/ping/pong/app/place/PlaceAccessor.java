package org.dan.ping.pong.app.place;

import org.dan.ping.pong.app.tournament.DbUpdaterFactory;
import org.dan.ping.pong.app.tournament.SeqAccessor;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Inject;

public class PlaceAccessor extends SeqAccessor<Pid, PlaceMemState> {
    @Inject
    public PlaceAccessor(
            PlaceService cache,
            DbUpdaterFactory dbUpdaterFactory,
            SequentialExecutor sequentialExecutor,
            PlatformTransactionManager tx) {
        super(cache, dbUpdaterFactory, sequentialExecutor, tx);
    }
}
