package org.dan.ping.pong.app.bid;

import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BidDao {
    void setBidState(int tid, int uid, BidState target,
            List<BidState> expected, Instant now, DbUpdater batch);

    void markParticipantsBusy(OpenTournamentMemState tournament,
            Collection<Integer> uids, Instant now, DbUpdater batch);

    void setGroupForUids(int gid, int tid, List<ParticipantMemState> groupBids);

    void enlist(ParticipantMemState bid, Instant now, Optional<Integer> providedRank, DbUpdater batch);

    List<ParticipantState> findEnlisted(int tid);

    Optional<DatedParticipantState> getParticipantInfo(int tid, int uid);

    void setCategory(SetCategory setCategory, Instant now, DbUpdater batch);

    void resetStateByTid(int tid, Instant now, DbUpdater batch);

    Map<Integer, ParticipantMemState> loadParticipants(Tid tid);
}
