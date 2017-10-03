package org.dan.ping.pong.app.server.castinglots;

import static org.dan.ping.pong.app.server.match.MatchJerseyTest.RULES_G2Q1_S1A2G11_PRNK;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.ProvidedRank.R1;
import static org.dan.ping.pong.mock.simulator.ProvidedRank.R2;
import static org.dan.ping.pong.mock.simulator.ProvidedRank.R3;
import static org.dan.ping.pong.mock.simulator.ProvidedRank.R4;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.server.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class ProvidedRankingJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void providedStrictRanking() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_G2Q1_S1A2G11_PRNK)
                .category(c1, p1, p2, p3, p4)
                .rank(R4, p1)
                .rank(R3, p4)
                .rank(R2, p3)
                .rank(R1, p2)
                .win(p3, p2)
                .win(p1, p4)
                .quitsGroup(p1, p3)
                .win(p1, p3)
                .champions(c1, p1, p3)
                .name("providedStrictRanking");

        simulator.simulate(scenario);
    }
}
