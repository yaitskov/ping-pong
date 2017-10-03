package org.dan.ping.pong.app.server.tournament;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.server.place.PlaceLink;

import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
public class MyTournamentInfo {
    private int tid;
    private String name;
    private TournamentState state;
    private PlaceLink place;
    private Optional<Double> price;
    private Instant opensAt;
    private int categories;
    private int enlisted;
    private Optional<Integer> previousTid = Optional.empty();
}
