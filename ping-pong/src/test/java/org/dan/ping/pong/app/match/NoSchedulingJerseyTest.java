package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S3A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class NoSchedulingJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void noScheduling() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .tables(0)
                .name("noScheduling")
                .rules(RULES_G8Q1_S3A2G11.withPlace(Optional.empty()))
                .category(c1, p1, p2, p3)
                .win(p1, p2)
                .win(p1, p3)
                .win(p2, p3)
                .quitsGroup(p1)
                .champions(c1, p1);
        simulator.simulate(scenario);
    }
}
