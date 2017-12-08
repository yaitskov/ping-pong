package org.dan.ping.pong.app.castinglots;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.rank.OrderDirection;
import org.dan.ping.pong.app.tournament.Tid;

import java.util.List;
import java.util.Set;

public interface CastingLotsDaoIf {
    List<Uid> loadSeed(Tid tid, Set<Uid> uids);
    List<Uid> loadRanks(Tid tid, Set<Uid> uids, OrderDirection direction);
}
