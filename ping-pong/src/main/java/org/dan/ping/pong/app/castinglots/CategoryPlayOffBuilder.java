package org.dan.ping.pong.app.castinglots;

import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;

public interface CategoryPlayOffBuilder {
    void build(TournamentMemState tournament, Integer cid,
            List<ParticipantMemState> bids, DbUpdater batch);
}
