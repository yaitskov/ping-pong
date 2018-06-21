package org.dan.ping.pong.app.match.dispute;

import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.db.DbUpdater;

public interface MatchDisputeDao {
    void create(Tid tid, DisputeMemState dispute, DbUpdater batch);
}
