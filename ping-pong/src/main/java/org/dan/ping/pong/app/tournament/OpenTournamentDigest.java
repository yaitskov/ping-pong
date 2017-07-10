package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenTournamentDigest {
    private int tid;
    private String name;
    private int gamesComplete;
    private int gamesOverall;
    private Instant startedAt;
    private int pariticipants;
}
