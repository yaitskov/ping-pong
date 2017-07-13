package org.dan.ping.pong.simulate;

import static org.dan.ping.pong.mock.simulator.AutoResolution.RANDOM;
import static org.dan.ping.pong.mock.simulator.Pause.BeforeScore;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_1_G_2;

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
public class OneGroup4PlayersSim {
    @Inject
    private Simulator simulator;

    @Test
    @SneakyThrows
    public void pauseBeforeSecondScore() {
        simulator.simulate(T_1_Q_1_G_2,
                TournamentScenario.begin()
                        .autoResolution(RANDOM)
                        .name("pauseBeforeSecondScore")
                        .category(c1, p1, p2, p3, p4)
                        .win(p1, p2)
                        .pause(p1, p3, BeforeScore)
                        .win(p1, p3));
    }
}
