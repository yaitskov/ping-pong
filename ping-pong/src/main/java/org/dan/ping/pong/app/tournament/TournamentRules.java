package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRules {
    private MatchValidationRule match;
    private GroupRules group;
    private int prizeWinningPlaces = 3;
    private int thirdPlaceMatch;
}
