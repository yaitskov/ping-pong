package org.dan.ping.pong.app.tournament.console;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Set;

public interface ConsoleStrategy {
    void onGroupComplete(int gid, TournamentMemState tournament, Set<Uid> quitUids, DbUpdater batch);
}
