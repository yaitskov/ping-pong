package org.dan.ping.pong.app.tournament.marshaling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TournamentEnvelope {
    private Instant exportedAt;
    private ExportedTournament tournament;
}
