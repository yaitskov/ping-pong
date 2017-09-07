package org.dan.ping.pong.mock.simulator;

import static org.dan.ping.pong.mock.simulator.MatchOutcome.OUTCOMES;

import java.util.Set;

public enum AutoResolution {
    RANDOM {
        @Override
        public MatchOutcome choose(Set<Player> player) {
            int v = (int) (Math.random() * 10 * OUTCOMES.size());
            return OUTCOMES.get(v % OUTCOMES.size()) ;
        }
    };


    public abstract MatchOutcome choose(Set<Player> player);
}
