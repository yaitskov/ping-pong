package org.dan.ping.pong.app.score;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.tournament.Tid;

import java.util.List;
import java.util.Map;

public interface MatchScoreDao {
    Map<Mid, Map<Bid, List<Integer>>> load(Tid tid);
}
