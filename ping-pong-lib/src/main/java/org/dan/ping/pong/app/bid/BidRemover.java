package org.dan.ping.pong.app.bid;

import static java.util.stream.Collectors.toList;

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

    public void removeByCategory(TournamentMemState tournament, int cid, DbUpdater batch) {
        // remove all disputes
        matchRemover.removeByCategory(tournament, cid, batch);

        final List<Bid> toBeRemoveBids = tournament.findBidsByCategory(cid)
                .map(ParticipantMemState::getBid)
                .collect(toList());
        bidDao.removeByIds(tournament.getTid(), toBeRemoveBids, batch);
        toBeRemoveBids.forEach(uid -> tournament.getParticipants().remove(uid));
    }
}
