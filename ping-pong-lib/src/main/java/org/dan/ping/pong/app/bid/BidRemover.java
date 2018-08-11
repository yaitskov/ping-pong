package org.dan.ping.pong.app.bid;

import static java.util.stream.Collectors.toList;

import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.match.MatchRemover;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;

import javax.inject.Inject;

public class BidRemover {
    @Inject
    private BidDao bidDao;

    @Inject
    private MatchRemover matchRemover;

    public void removeByCategory(TournamentMemState tournament, Cid cid, DbUpdater batch) {
        // remove all disputes
        matchRemover.removeByCategory(tournament, cid, batch);

        final List<Bid> toBeRemoveBids = tournament.findBidsByCategory(cid)
                .map(ParticipantMemState::getBid)
                .collect(toList());
        removeByBids(tournament, batch, toBeRemoveBids);
    }

    public void removeByBids(TournamentMemState tournament, DbUpdater batch, List<Bid> toBeRemoveBids) {
        bidDao.removeByIds(tournament.getTid(), toBeRemoveBids, batch);
        toBeRemoveBids.forEach(uid -> tournament.getParticipants().remove(uid));
    }
}
