package org.dan.ping.pong.app.tournament;

import org.dan.ping.pong.app.place.PlaceLink;

import java.time.Instant;
import java.util.Optional;

public class TournamentGeneralInfo {
    private int tid;
    private Instant opensAt;
    private PlaceLink placeLink;
    private String name;
    private Optional<Integer> previousTid = Optional.empty();
    private Optional<Double> ticketPrice = Optional.empty();

}
