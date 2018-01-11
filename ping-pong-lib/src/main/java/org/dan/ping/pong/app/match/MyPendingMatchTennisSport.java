package org.dan.ping.pong.app.match;

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
public class MyPendingMatchTennisSport implements MyPendingMatchSport {
    private int minGamesToWin;
    private int minAdvanceInGames;
    private int superTieBreakGames;
    private int setsToWin;
}
