package org.dan.ping.pong.mock;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.tournament.TournamentState;

@Getter
@Wither
@Builder
public class TournamentProps {
    private int maxGroupSize;
    private int quitsFromGroup;
    private TournamentState state;

    public static class TournamentPropsBuilder {
        private int quitsFromGroup = 2;
        private TournamentState state = TournamentState.Draft;
        private int maxGroupSize = 8;
    }
}
