package org.dan.ping.pong.app.tournament.console;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.group.Gid;
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
    public void onGroupComplete(Gid gid, TournamentMemState tournament,
            Set<Bid> loserBids, DbUpdater batch) {
        noConsoleStrategy.onGroupComplete(gid, tournament, loserBids, batch);
        if (tournament.getRule().consoleP()) {
            consoleStrategy.onGroupComplete(gid, tournament, loserBids, batch);
        }
    }
}
