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
import org.dan.ping.pong.app.sport.tennis.TennisFamilyRules;

@Getter
@Setter
@Wither
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PingPongMatchRules implements MatchRules, TennisFamilyRules {
    public static final String MGTW = "mgtw";
    public static final String MAIG = "maig";
    public static final String MPG = "mpg";
    public static final String STW = "stw";
    public static final String COS = "cos";
    public static final String BALLS_CANNOT_BE_LESS_THAN = "Balls cannot be less than";
    public static final String WINNER_SHOULD_HAVE_AT_LEAST_N_BALLS = "Winner should have at least n balls";
    public static final String DIFFERENCE_BETWEEN_BALLS_CANNOT_BE_LESS_THAN = "Difference between balls cannot be less than";
    public static final String WINNER_HAS_TO_MUCH_BALLS = "Winner has to much balls";

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

    @Override
    public String errorGamesCannotBeLessThan() {
        return BALLS_CANNOT_BE_LESS_THAN;
    }

    @Override
    public String errorWinnerShouldHaveAtLeast() {
        return WINNER_SHOULD_HAVE_AT_LEAST_N_BALLS;
    }

    @Override
    public String errorDifferenceBetweenGamesCannotBeLessThan() {
        return DIFFERENCE_BETWEEN_BALLS_CANNOT_BE_LESS_THAN;
    }

    @Override
    public String errorWinnerHasToMuchGames() {
        return WINNER_HAS_TO_MUCH_BALLS;
    }
}
