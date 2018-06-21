package org.dan.ping.pong.app.castinglots;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.rank.OrderDirection;
import org.dan.ping.pong.app.match.MatchTag;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CastingLotsDaoIf {
    List<Uid> loadSeed(Tid tid, Set<Uid> uids);

    int generateGroupMatches(DbUpdater batch, TournamentMemState tournament, int gid,
            List<ParticipantMemState> groupBids, int priorityGroup,
            Optional<MatchTag> tag);

    List<Uid> loadRanks(Tid tid, Set<Uid> uids, OrderDirection direction);
}
