package org.dan.ping.pong.app.tournament;

import static lombok.AccessLevel.PRIVATE;
import static org.dan.ping.pong.app.sport.SportType.PingPong;
import static org.dan.ping.pong.app.tournament.TournamentState.Hidden;
import static org.dan.ping.pong.app.tournament.TournamentType.Classic;
import static org.dan.ping.pong.app.tournament.TournamentType.Console;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.tournament.marshaling.ExportedTournament;

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
    private TournamentState state = Hidden;

    public static class CreateTournamentBuilder {
        Optional<Double> ticketPrice = Optional.empty();
        Optional<Tid> previousTid = Optional.empty();
        SportType sport = PingPong;
        TournamentType type = Classic;
        TournamentState state = Hidden;
    }

    public void validate() {
        if (this.getRules() == null) {
            throw badRequest("No rules");
        }
        if (sport != rules.getMatch().sport()) {
            throw badRequest("sport type mismatch");
        }
    }

    public void validateNew() {
        validate();
        if (type != Classic) {
            throw badRequest("Tournament type is not Classic but " + type);
        }
        if (state != Hidden) {
            throw badRequest("Tournament state is not Hidden");
        }
    }

    public static CreateTournament ofImport(Pid placeId, ExportedTournament tournament) {
        return CreateTournament.builder()
                .name(tournament.getName())
                .sport(tournament.getSport())
                .placeId(placeId)
                .opensAt(tournament.getOpensAt())
                .ticketPrice(tournament.getTicketPrice())
                .rules(tournament.getRule())
                .type(tournament.getType())
                .state(tournament.getState())
                .build();
    }
}
