package org.dan.ping.pong.mock.simulator;

import java.util.Map;

public interface SetGenerator {
    Map<Player, Integer> generate();
    boolean isEmpty();
    int getSetNumber();
}
