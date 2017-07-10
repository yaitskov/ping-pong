package org.dan.ping.pong.app.match;

public enum MatchState {
    Draft, // initial - no all participant are known yet
    Place, // not free place - al tables are busy
    Game,  // the participants are playing right now
    Over   // out of the game is known
}
