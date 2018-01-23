package org.dan.ping.pong.app.tournament.console;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Rest;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.match.MatchService.isPyrrhic;

import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Set;

import javax.inject.Inject;

public class NoConsoleStrategy implements ConsoleStrategy {
    @Inject
    private BidService bidService;

    @Override
    public void onGroupComplete(int gid, TournamentMemState tournament, Set<Uid> quitUids, DbUpdater batch) {
        tournament.getParticipants().values().stream()
                .filter(bid -> bid.getGid().equals(of(gid)))
                .filter(bid -> !quitUids.contains(bid.getUid()))
                .filter(bid -> !isPyrrhic(bid))
                .forEach(bid -> bidService.setBidState(bid, Lost, asList(Wait, Rest), batch));
    }
}
