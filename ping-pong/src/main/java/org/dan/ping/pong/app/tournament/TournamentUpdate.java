package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.place.Pid;

import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentUpdate {
    private Tid tid;
    private String name;
    private Instant opensAt;
    private Pid placeId;
    private Optional<Double> price;
}
