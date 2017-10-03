package org.dan.ping.pong.app.server.match;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.sys.error.Error;

@Getter
@NoArgsConstructor
public class MatchScoredError extends Error {
    private final String error = "matchScored";
    @Setter
    private MatchScore matchScore;

    public MatchScoredError(MatchScore matchScore) {
        super("Match is already scored");
        this.matchScore = matchScore;
    }
}
