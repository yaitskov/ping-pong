package org.dan.ping.pong.app.server.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.server.tournament.SetScoreResultName;

import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetScoreResult {
    private SetScoreResultName scoreOutcome;
    private Optional<Integer> nextSetNumberToScore;
    private Optional<MatchScore> matchScore;

    public static class SetScoreResultBuilder {
        private Optional<Integer> nextSetNumberToScore = Optional.empty();
        private Optional<MatchScore> matchScore = Optional.empty();
    }
}
