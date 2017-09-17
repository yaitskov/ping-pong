package org.dan.ping.pong.mock;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.tournament.TournamentState;

import java.time.Instant;
import java.util.Optional;

@Getter
@Wither
@Builder
public class TournamentProps {
    private TournamentRules rules;
    private TournamentState state;
    private Optional<Instant> opensAt;

    public static class TournamentPropsBuilder {
        private TournamentState state = TournamentState.Draft;
        private Optional<Instant> opensAt = Optional.empty();
    }
}
