package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.COS;
import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.MAIG;
import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.MGTW;
import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.STW;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MyPendingMatchPingPongSport implements MyPendingMatchSport {
    @JsonProperty(MGTW)
    private int minGamesToWin;
    @JsonProperty(MAIG)
    private int minAdvanceInGames;
    @JsonProperty(COS)
    private boolean countOnlySets;
    @JsonProperty(STW)
    private int setsToWin;
}
