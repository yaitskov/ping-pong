package org.dan.ping.pong.app.tournament;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TournamentRules {
    private MatchValidationRule match;
    private GroupRules group;
    private int prizeWinningPlaces;
}
