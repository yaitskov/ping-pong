package org.dan.ping.pong.mock.simulator;

import static org.dan.ping.pong.app.tournament.PingPongMatchRulesUnitTest.PING_PONG_RULE;
import static org.dan.ping.pong.mock.simulator.MatchOutcome.W30;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.createRndGen;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.dan.ping.pong.app.tournament.TournamentRules;
import org.junit.Test;

public class RndSetGeneratorTest {
    @Test
    public void complete() {
        final RndSetGenerator generator = createRndGen(p1, W30, p2);
        assertFalse(generator.isEmpty());
        final TournamentScenario ts = TournamentScenario.begin()
                .rules(TournamentRules.builder().match(PING_PONG_RULE).build());
        generator.generate(ts);
        generator.generate(ts);
        generator.generate(ts);
        assertTrue(generator.isEmpty());
    }
}
