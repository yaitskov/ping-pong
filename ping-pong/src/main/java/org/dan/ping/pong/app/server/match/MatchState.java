package org.dan.ping.pong.app.server.match;

public enum MatchState {
    Draft, // initial - no all participant are known yet
    Place, // not free place - al tables are busy
    Game,  // the participants are playing right now
    Auto,  // a participant resigned before its opponent was detected
    Over   // out of the game is known
}
