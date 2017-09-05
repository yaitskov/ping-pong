package org.dan.ping.pong.app.tournament;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TournamentRules {
    private MatchValidationRule match;
    private GroupRules group;
    private int prizeWinningPlaces;
}
