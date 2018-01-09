package org.dan.ping.pong.app.sport.pingpong;

import static org.dan.ping.pong.app.sport.SportType.PingPong;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@NoArgsConstructor
@AllArgsConstructor
public class PingPongMatchRules implements MatchRules {
    private int minGamesToWin;
    private int minAdvanceInGames;
    private int minPossibleGames;
    private int setsToWin;

    public SportType sport() {
        return PingPong;
    }

    public MyPendingMatchSport toMyPendingMatchSport() {
        return MyPendingMatchPingPongSport.builder()
                .minGamesToWin(minGamesToWin)
                .minAdvanceInGames(minAdvanceInGames)
                .build();
    }
}
