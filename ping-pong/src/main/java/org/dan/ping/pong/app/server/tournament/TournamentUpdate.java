package org.dan.ping.pong.app.server.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentUpdate {
    private int tid;
    private String name;
    private Instant opensAt;
    private int placeId;
    private Optional<Double> price;
}
