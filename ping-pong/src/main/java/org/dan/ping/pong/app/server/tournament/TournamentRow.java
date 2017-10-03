package org.dan.ping.pong.app.server.tournament;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.server.match.Pid;

import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@Builder
public class TournamentRow {
    private Tid tid;
    private Pid pid;
    private String name;
    private Instant startedAt;
    private Optional<Instant> endedAt;
    private TournamentState state;
    private TournamentRules rules;
}
