package org.dan.ping.pong.app.sport.pingpong;

import static org.dan.ping.pong.app.sport.SportType.PingPong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.match.MyPendingMatchPingPongSport;
import org.dan.ping.pong.app.match.MyPendingMatchSport;
import org.dan.ping.pong.app.sport.MatchRules;
import org.dan.ping.pong.app.sport.SportType;

@Getter
@Setter
@Wither
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PingPongMatchRules implements MatchRules {
    private int minGamesToWin;
    private int minAdvanceInGames;
    private int minPossibleGames;
    private int setsToWin;
    private boolean countOnlySets;

    public SportType sport() {
        return PingPong;
    }

    public MyPendingMatchSport toMyPendingMatchSport() {
        if (countOnlySets) {
            return MyPendingMatchPingPongSport.builder()
                    .countOnlySets(countOnlySets)
                    .setsToWin(setsToWin)
                    .build();
        }
        return MyPendingMatchPingPongSport.builder()
                .minGamesToWin(minGamesToWin)
                .minAdvanceInGames(minAdvanceInGames)
                .build();
    }

    @Override
    public boolean countOnlySets() {
        return countOnlySets;
    }
}
