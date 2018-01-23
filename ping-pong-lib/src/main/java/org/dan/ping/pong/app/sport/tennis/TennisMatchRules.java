package org.dan.ping.pong.app.sport.tennis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.match.MyPendingMatchSport;
import org.dan.ping.pong.app.match.MyPendingMatchTennisSport;
import org.dan.ping.pong.app.sport.MatchRules;
import org.dan.ping.pong.app.sport.SportType;

@Getter
@Setter
@Wither
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TennisMatchRules implements MatchRules {
    private int minGamesToWin; // 6
    private int minAdvanceInGames; // 2
    private int minPossibleGames; // 0
    private int setsToWin; // 2
    private int superTieBreakGames;  // 10

    public SportType sport() {
        return SportType.Tennis;
    }

    @Override
    public MyPendingMatchSport toMyPendingMatchSport() {
        return MyPendingMatchTennisSport.builder()
                .minAdvanceInGames(minAdvanceInGames)
                .setsToWin(setsToWin)
                .minGamesToWin(minGamesToWin)
                .superTieBreakGames(superTieBreakGames)
                .build();
    }
}
