package org.dan.ping.pong.app.tournament;

import org.dan.ping.pong.app.bid.Uid;

import java.util.Optional;
import java.util.Set;

public interface TournamentDao {
    Set<Uid> loadAdmins(Tid tid);

    Optional<TournamentRow> getRow(Tid tid);
}
