package org.dan.ping.pong.app.tournament;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.sport.SportType;

import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@Builder
public class TournamentRow {
    private Tid tid;
    private Pid pid;
    private SportType sport;
    private String name;
    private TournamentType type;
    private Instant startedAt;
    private Optional<Instant> endedAt;
    private TournamentState state;
    private TournamentRules rules;
    private Optional<Double> ticketPrice;
    private Optional<Tid> previousTid;
    private Optional<Tid> consoleTid;
    private Optional<Tid> masterTid;
}
