package org.dan.ping.pong.app.match;

import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;

public class MatchRulesConst {
    public static final PingPongMatchRules S1A2G11 = PingPongMatchRules.builder()
            .setsToWin(1)
            .minAdvanceInGames(2)
            .minPossibleGames(0)
            .minGamesToWin(11)
            .build();
    public static final PingPongMatchRules S1 = S1A2G11.withCountOnlySets(true);
    public static final PingPongMatchRules S3A2G11 = PingPongMatchRules.builder()
            .setsToWin(3)
            .minAdvanceInGames(2)
            .minPossibleGames(0)
            .minGamesToWin(11)
            .build();
    public static final PingPongMatchRules S3 = S3A2G11.withCountOnlySets(true);
    public static final PingPongMatchRules S3A2G11_COS = S3A2G11.withCountOnlySets(true);
    public static final PingPongMatchRules S2A2G11 = S3A2G11.withSetsToWin(2);
}
