package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MyRecentJudgedTournaments {
    private Optional<DatedTournamentDigest> next;
    private List<DatedTournamentDigest> current;
    private Optional<DatedTournamentDigest> previous;
}
