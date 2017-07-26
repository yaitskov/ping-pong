package org.dan.ping.pong.mock;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.tournament.TournamentState;

import java.time.Instant;
import java.util.Optional;

@Getter
@Wither
@Builder
public class TournamentProps {
    private int maxGroupSize;
    private int quitsFromGroup;
    private int matchScore;
    private TournamentState state;
    private Optional<Instant> opensAt;

    public static class TournamentPropsBuilder {
        private int quitsFromGroup = 2;
        private int matchScore = 3;
        private TournamentState state = TournamentState.Draft;
        private int maxGroupSize = 8;
        private Optional<Instant> opensAt = Optional.empty();
    }
}
