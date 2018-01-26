package org.dan.ping.pong.app.tournament.console;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.springframework.context.annotation.Primary;

import java.util.Set;

import javax.inject.Inject;

@Primary
public class ConsoleStrategyDispatcher implements ConsoleStrategy {
    @Inject
    private NoConsoleStrategy noConsoleStrategy;

    @Inject
    private ConsoleStrategyImpl consoleStrategy;

    @Override
    public void onGroupComplete(int gid, TournamentMemState tournament, Set<Uid> loserUids, DbUpdater batch) {
        if (tournament.getRule().consoleP()) {
            consoleStrategy.onGroupComplete(gid, tournament, loserUids, batch);
        }
        noConsoleStrategy.onGroupComplete(gid, tournament, loserUids, batch);
    }
}
