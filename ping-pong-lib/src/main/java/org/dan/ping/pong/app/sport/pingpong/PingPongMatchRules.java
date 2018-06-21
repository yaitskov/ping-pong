package org.dan.ping.pong.app.sport.pingpong;

import static org.dan.ping.pong.app.sport.SportType.PingPong;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    public static final String MGTW = "mgtw";
    public static final String MAIG = "maig";
    public static final String MPG = "mpg";
    public static final String STW = "stw";
    public static final String COS = "cos";

    @JsonProperty(MGTW)
    private int minGamesToWin;
    @JsonProperty(MAIG)
    private int minAdvanceInGames;
    @JsonProperty(MPG)
    private int minPossibleGames;
    @JsonProperty(STW)
    private int setsToWin;
    @JsonProperty(COS)
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
