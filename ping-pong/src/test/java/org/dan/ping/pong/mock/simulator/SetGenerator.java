package org.dan.ping.pong.mock.simulator;

import java.util.Map;

public interface SetGenerator {
    Player getPlayerA();
    Player getPlayerB();
    Map<Player, Integer> generate(TournamentScenario scenario);
    boolean isEmpty();
    int getSetNumber();
    default void setMid(int mid) {
        // skip
    }
}
