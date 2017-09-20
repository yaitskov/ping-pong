package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G2Q1_S1A2G11_MIX;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class BalancedMixingRankingJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void mixGroupDivision() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_G2Q1_S1A2G11_MIX)
                .category(c1, p1, p2, p3, p4)
                .win(p1, p4)
                .win(p2, p3)
                .quitsGroup(p1, p2)
                .win(p1, p2)
                .champions(c1, p1, p2)
                .name("mixGroupDivision");

        simulator.simulate(scenario);
    }
}
