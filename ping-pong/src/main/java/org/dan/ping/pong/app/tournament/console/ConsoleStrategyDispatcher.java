package org.dan.ping.pong.app.tournament.console;

import static org.dan.ping.pong.app.group.ConsoleTournament.NO;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.ConsoleTournament;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.playoff.PlayOffRule;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
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
        if (tournament.getRule().consoleInGroupP()) {
            consoleStrategy.onGroupComplete(gid, tournament, loserBids, batch);
        }
    }

    @Override
    public void onPlayOffCategoryComplete(
            Cid cid, TournamentMemState tournament, DbUpdater batch) {
        final ConsoleTournament playOffConsole = tournament.getRule()
                .getPlayOff()
                .map(PlayOffRule::getConsole)
                .orElse(NO);
        if (playOffConsole == NO) {
            return;
        }
        consoleStrategy.onPlayOffCategoryComplete(cid, tournament, batch);
    }

    @Override
    public void onParticipantLostPlayOff(
            TournamentMemState tournament,
            ParticipantMemState lostParticipant, DbUpdater batch) {
        final ConsoleTournament playOffConsole = tournament.getRule()
                .getPlayOff()
                .map(PlayOffRule::getConsole)
                .orElse(NO);
        if (playOffConsole == NO) {
            return;
        }
        consoleStrategy.onParticipantLostPlayOff(tournament, lostParticipant, batch);
    }
}
