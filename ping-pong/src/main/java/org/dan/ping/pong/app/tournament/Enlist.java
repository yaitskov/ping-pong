package org.dan.ping.pong.app.tournament;

import org.dan.ping.pong.app.category.Cid;

import java.util.Optional;

public interface Enlist {
    Cid getCid();
    Optional<Integer> getProvidedRank();
}
