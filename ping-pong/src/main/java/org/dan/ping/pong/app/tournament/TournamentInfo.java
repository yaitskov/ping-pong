package org.dan.ping.pong.app.tournament;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class TournamentInfo {
    private int tid;
    private int quitesFromGroup;
    private int maxGroupSize;
    private int thirdPlaceMath;
    private TournamentState state;
}
