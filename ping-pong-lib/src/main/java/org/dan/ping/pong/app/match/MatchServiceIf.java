package org.dan.ping.pong.app.match;

import org.dan.ping.pong.app.category.SelectedCid;

import java.util.Optional;

public interface MatchServiceIf {
    Mid createPlayOffMatch(SelectedCid sCid,
            Optional<Mid> winMid, Optional<Mid> loserMid,
            int priority, int level, MatchType type,
            Optional<MatchTag> oTag);
}
