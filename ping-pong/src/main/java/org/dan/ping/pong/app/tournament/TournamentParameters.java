package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentParameters {
    private int tid;
    private int matchScore;
    private int maxGroupSize;
    private int quitsGroup;
    private int thirdPlaceMatch;
}
