package org.dan.ping.pong.app.tournament.console;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Rest;
import static org.dan.ping.pong.app.bid.BidState.Wait;

import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.sys.error.PiPoEx;

import java.util.Set;

import javax.inject.Inject;

public class NoConsoleStrategy implements ConsoleStrategy {
    @Inject
    private BidService bidService;

    private static final Set<BidState> terminalStates = ImmutableSet.of(Quit, Expl, Lost);

    @Override
    public void onGroupComplete(Gid gid, TournamentMemState tournament,
            Set<Bid> loserBids, DbUpdater batch) {
        loserBids.stream()
                .map(tournament::getParticipant)
                .filter(bid -> !terminalStates.contains(bid.getBidState()))
                .forEach(bid -> bidService.setBidState(bid, Lost, asList(Wait, Rest), batch));
    }

    @Override
    public void onPlayOffCategoryComplete(
            Cid cid, TournamentMemState tournament, DbUpdater batch) {
        throw PiPoEx.internalError("dead");
    }

    @Override
    public void onParticipantLostPlayOff(
            TournamentMemState tournament, ParticipantMemState lostParticipant, DbUpdater batch) {
        throw PiPoEx.internalError("dead");
    }
}
