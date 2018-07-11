package org.dan.ping.pong.app.sport.tennis;

import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.MAIG;
import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.MGTW;
import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.MPG;
import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.STW;

import com.fasterxml.jackson.annotation.JsonProperty;
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

import java.util.Optional;

@Getter
@Setter
@Wither
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TennisMatchRules implements MatchRules {
    public static final String STBG = "stbg";
    public static final String GAMES_CANNOT_BE_LESS_THAN = "Games cannot be less than";
    public static final String DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_LESS_THAN = "Difference between games cannot be less than";
    public static final String WINNER_HAS_TO_MUCH_GAMES = "Winner has to much games";

    @JsonProperty(MGTW)
    private int minGamesToWin; // 6
    @JsonProperty(MAIG)
    private int minAdvanceInGames; // 2
    @JsonProperty(MPG)
    private int minPossibleGames; // 0
    @JsonProperty(STW)
    private int setsToWin; // 2
    @JsonProperty(STBG)
    private Optional<Integer> superTieBreakGames = Optional.empty();  // 10

    public static class TennisMatchRulesBuilder {
        Optional<Integer> superTieBreakGames = Optional.empty();
    }

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

    @Override
    public boolean countOnlySets() {
        return false;
    }

    public boolean isSuperTieBreak(int iSet) {
        return iSet >= setsToWin;
    }
}
