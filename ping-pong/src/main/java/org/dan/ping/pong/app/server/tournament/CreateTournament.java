package org.dan.ping.pong.app.server.tournament;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class CreateTournament {
    private Instant opensAt;
    private String name;
    private Optional<Integer> previousTid;
    private int placeId;
    private TournamentRules rules;
    private Optional<Double> ticketPrice;
}
