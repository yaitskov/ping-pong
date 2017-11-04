package org.dan.ping.pong.app.match.dispute;

import org.dan.ping.pong.app.tournament.Tid;

public interface MatchDisputeDao {
    DisputeId create(Tid tid, DisputeMemState dispute);
}
