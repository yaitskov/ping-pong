package org.dan.ping.pong.app.bid;

import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;

import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.collection.MaxValue;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface BidDao {
    Set<BidState> TERMINAL_BID_STATES = ImmutableSet.of(Win1, Win2, Win3,
            Expl, Lost, Quit);

    void setBidState(Tid tid, Bid bid, BidState target,
            Collection<BidState> expected, Instant now, DbUpdater batch);

    void markParticipantsBusy(TournamentMemState tournament,
            Collection<Bid> bids, Instant now, DbUpdater batch);

    void setGroupForUids(DbUpdater batch, int gid, Tid tid, List<ParticipantMemState> groupBids);

    void enlist(ParticipantMemState bid, Optional<Integer> providedRank, DbUpdater batch);

    void setCategory(SetCategory setCategory, Instant now, DbUpdater batch);

    void resetStateByTid(Tid tid, Instant now, DbUpdater batch);

    Map<Bid, ParticipantMemState> loadParticipants(Tid tid, MaxValue<Bid> maxBid);

    void renameParticipant(Uid uid, String newName, DbUpdater batch);

    void removeByIds(Tid tid, List<Bid> toBeRemoveBids, DbUpdater batch);
}
