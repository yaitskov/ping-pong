package org.dan.ping.pong.simulate;

import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.Player.p7;
import static org.dan.ping.pong.mock.simulator.Player.p8;
import static org.dan.ping.pong.mock.simulator.Player.p9;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c2;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c4;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_3_Q_1_G_8;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimulationCtx.class})
public class FourCategoriesSim {
    @Inject
    private Simulator simulator;

    @Test
    @SneakyThrows
    public void justEnlistSmallestGroup() {
        simulator.simulate(T_3_Q_1_G_8,
                TournamentScenario.begin()
                        .doNotBegin()
                        .ignoreUnexpectedGames()
                        .name("justEnlistSmallestGroup")
                        .category(c1, p1, p2)
                        .category(c2, p3, p4)
                        .category(c3, p5, p6)
                        .category(c4, p7, p8, p9));
    }
}