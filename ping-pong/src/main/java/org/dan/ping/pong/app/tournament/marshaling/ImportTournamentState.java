package org.dan.ping.pong.app.tournament.marshaling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.place.Pid;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportTournamentState {
    private Pid placeId;
    private TournamentEnvelope tournament;
}
