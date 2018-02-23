package org.dan.ping.pong.app.match;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public enum MatchState {
    Draft, // initial - no all participant are known yet
    Place, // not free place - al tables are busy
    Game,  // the participants are playing right now
    Auto,  // a participant resigned before its opponent was detected
    Over   // out of the game is known
    ;

    public static final Set<MatchState> INCOMPLETE_MATCH_STATES = ImmutableSet.of(Draft, Place, Game);
}
