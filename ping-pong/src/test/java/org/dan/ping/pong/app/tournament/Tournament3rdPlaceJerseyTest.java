package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.Player.p7;
import static org.dan.ping.pong.mock.simulator.Player.p8;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_1_G_2_3P;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class Tournament3rdPlaceJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Inject
    private BidDao bidDao;

    @Test
    public void play3rdPlaceGame() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("play3rdPlaceGame")
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8)
                .w30(p1, p2)
                .w30(p3, p4)
                .w30(p5, p6)
                .w30(p7, p8)
                .quitsGroup(p1, p3, p5, p7)
                .w30(p1, p3)
                .w30(p5, p7)
                .w30(p1, p5)
                .w31(p3, p7)
                .champions(c1, p1, p5, p3);

        simulator.simulate(T_1_Q_1_G_2_3P, scenario);
    }
}
