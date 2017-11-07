package org.dan.ping.pong.app.tournament;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.place.PlaceLink;

import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
public class MyTournamentInfo {
    private Tid tid;
    private String name;
    private TournamentState state;
    private PlaceLink place;
    private Optional<Double> price;
    private Instant opensAt;
    private int categories;
    private int enlisted;
    private Optional<Tid> previousTid = Optional.empty();
}
