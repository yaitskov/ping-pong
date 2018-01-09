package org.dan.ping.pong.app.sport.tennis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.sport.MatchRules;

@Getter
@Setter
@Wither
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TennisMatchRules implements MatchRules {
    private int minGamesToWin; // 6
    private int minAdvanceInGames; // 2
    private int minPossibleGames; // 0
    private int setsToWin; // 2
    private int tieBreakGames;  // 10
}
