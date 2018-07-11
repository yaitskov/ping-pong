package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.MAIG;
import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.MGTW;
import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.STW;
import static org.dan.ping.pong.app.sport.tennis.TennisMatchRules.STBG;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MyPendingMatchTennisSport implements MyPendingMatchSport {
    @JsonProperty(MGTW)
    private int minGamesToWin;
    @JsonProperty(MAIG)
    private int minAdvanceInGames;
    @JsonProperty(STBG)
    private Optional<Integer> superTieBreakGames = Optional.empty();
    @JsonProperty(STW)
    private int setsToWin;


    public static class MyPendingMatchTennisSportBuilder {
        Optional<Integer> superTieBreakGames = Optional.empty();
    }
}
