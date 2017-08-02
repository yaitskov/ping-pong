package org.dan.ping.pong.simulate;

import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c2;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_1_G_8;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.mock.simulator.EnlistMode;
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
public class TwoCategoriesSim {
    @Inject
    private Simulator simulator;

    @Test
    @SneakyThrows
    public void enlist3To1GrpAnd1ToAnother() {
        simulator.simulate(T_1_Q_1_G_8,
                TournamentScenario.begin()
                        .ignoreUnexpectedGames()
                        .name("enlist3To1GrpAnd1ToAnother")
                        .category(c1, p1, p2, p3)
                        .category(c2, p4)
                        .doNotBegin()
                        .presence(EnlistMode.Enlist, p4)
                        .presence(EnlistMode.Pass, p1, p2, p3));
    }
}
