package org.dan.ping.pong.app.match;

import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Optional;

public interface MatchServiceIf {
    Mid createPlayOffMatch(TournamentMemState tournament, Cid cid,
            Optional<Mid> winMid, Optional<Mid> loserMid,
            int priority, int level, MatchType type,
            Optional<MatchTag> oTag, DbUpdater batch);
}
