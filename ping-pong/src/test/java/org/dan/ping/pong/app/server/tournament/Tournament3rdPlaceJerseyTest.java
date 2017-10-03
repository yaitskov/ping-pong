package org.dan.ping.pong.app.server.tournament;

import static org.dan.ping.pong.app.server.match.MatchJerseyTest.RULES_G2Q1_S1A2G11_3P;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.Player.p7;
import static org.dan.ping.pong.mock.simulator.Player.p8;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.server.bid.BidDao;
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
        simulator.simulate(TournamentScenario.begin()
                .name("play3rdPlaceGame")
                .rules(RULES_G2Q1_S1A2G11_3P)
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8)
                .win(p1, p2)
                .win(p3, p4)
                .win(p5, p6)
                .win(p7, p8)
                .quitsGroup(p1, p3, p5, p7)
                .win(p1, p7)
                .win(p3, p5)
                .win(p7, p5)
                .win(p3, p1)
                .champions(c1, p3, p1, p7));
    }
}
