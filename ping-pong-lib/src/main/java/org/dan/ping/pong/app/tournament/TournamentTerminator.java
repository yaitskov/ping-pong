package org.dan.ping.pong.app.tournament;

import org.dan.ping.pong.sys.db.DbUpdater;

public interface TournamentTerminator {
    boolean endOfTournamentCategory(TournamentMemState tournament, int cid, DbUpdater batch);
}
