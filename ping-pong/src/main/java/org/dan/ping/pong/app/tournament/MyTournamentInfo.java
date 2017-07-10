package org.dan.ping.pong.app.tournament;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class MyTournamentInfo {
    private int tid;
    private String name;
    private int quitesFromGroup;
    private int maxGroupSize;
    private TournamentState state;
}
