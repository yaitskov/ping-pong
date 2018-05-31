package org.dan.ping.pong.app.bid;

import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;

import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface BidDao {
    Set<BidState> TERMINAL_BID_STATES = ImmutableSet.of(Win1, Win2, Win3,
            Expl, Lost, Quit);

    void setBidState(Tid tid, Uid uid, BidState target,
            Collection<BidState> expected, Instant now, DbUpdater batch);

    void markParticipantsBusy(TournamentMemState tournament,
            Collection<Uid> uids, Instant now, DbUpdater batch);

    void setGroupForUids(int gid, Tid tid, List<ParticipantMemState> groupBids);

    void setGroupForUids(DbUpdater batch, int gid, Tid tid, List<ParticipantMemState> groupBids);

    void enlist(ParticipantMemState bid, Optional<Integer> providedRank, DbUpdater batch);

    List<ParticipantState> findEnlisted(Tid tid);

    Optional<DatedParticipantState> getParticipantInfo(Tid tid, Uid uid);

    void setCategory(SetCategory setCategory, Instant now, DbUpdater batch);

    void resetStateByTid(Tid tid, Instant now, DbUpdater batch);

    Map<Uid, ParticipantMemState> loadParticipants(Tid tid);

    void renameParticipant(Uid uid, String newName, DbUpdater batch);
}
