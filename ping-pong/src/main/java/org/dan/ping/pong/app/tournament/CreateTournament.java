package org.dan.ping.pong.app.tournament;

import static lombok.AccessLevel.PRIVATE;
import static org.dan.ping.pong.app.sport.SportType.PingPong;
import static org.dan.ping.pong.app.tournament.TournamentType.Classic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.sport.SportType;

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
    private Optional<Tid> previousTid = Optional.empty();
    private Pid placeId;
    private TournamentRules rules;
    private Optional<Double> ticketPrice = Optional.empty();
    private SportType sport = PingPong;
    private TournamentType type = Classic;

    public static class CreateTournamentBuilder {
        Optional<Double> ticketPrice = Optional.empty();
        Optional<Tid> previousTid = Optional.empty();
        SportType sport = PingPong;
        TournamentType type = Classic;
    }
}
