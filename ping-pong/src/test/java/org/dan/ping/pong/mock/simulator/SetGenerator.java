package org.dan.ping.pong.mock.simulator;

import java.util.Map;

public interface SetGenerator {
    Player getPlayerA();
    Player getPlayerB();
    Map<Player, Integer> generate();
    boolean isEmpty();
    int getSetNumber();
}
