package org.dan.ping.pong.app.place;

import org.dan.ping.pong.app.match.Pid;
import org.dan.ping.pong.app.tournament.DbUpdaterFactory;
import org.dan.ping.pong.app.tournament.SeqAccessor;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;

import javax.inject.Inject;

public class PlaceAccessor extends SeqAccessor<Pid, PlaceMemState> {
    @Inject
    public PlaceAccessor(
            PlaceService cache,
            DbUpdaterFactory dbUpdaterFactory,
            SequentialExecutor sequentialExecutor) {
        super(cache, dbUpdaterFactory, sequentialExecutor);
    }
}
