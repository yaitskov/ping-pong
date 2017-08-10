package org.dan.ping.pong.app.match;

import lombok.Getter;
import org.dan.ping.pong.sys.error.Error;

@Getter
public class MatchScoredError extends Error {
    private final String error = "matchScored";

    public MatchScoredError() {
        super("Match is already scored");
    }
}
