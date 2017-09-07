package org.dan.ping.pong.mock.simulator;

import static org.dan.ping.pong.mock.simulator.MatchOutcome.W30;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.createRndGen;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RndSetGeneratorTest {
    @Test
    public void complete() {
        final RndSetGenerator generator = createRndGen(p1, W30, p2);
        assertFalse(generator.isEmpty());
        generator.generate();
        generator.generate();
        generator.generate();
        assertTrue(generator.isEmpty());
    }
}
