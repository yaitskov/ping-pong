package org.dan.ping.pong.simulate;

import static org.dan.ping.pong.mock.simulator.AutoResolution.RANDOM;
import static org.dan.ping.pong.mock.simulator.Pause.BeforeScore;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c2;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_1_G_8;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_3_Q_1_G_8;

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
public class OneGroup4PlayersSim {
    @Inject
    private Simulator simulator;

    @Test
    @SneakyThrows
    public void pauseBeforeSecondScore() {
        simulator.simulate(T_1_Q_1_G_8,
                TournamentScenario.begin()
                        .ignoreUnexpectedGames()
                        .autoResolution(RANDOM)
                        .name("pauseBeforeSecondScore")
                        .category(c1, p1, p2, p3, p4)
                        .win(p1, p2)
                        .pause(p1, p3, BeforeScore)
                        .win(p1, p3));
    }

    @Test
    @SneakyThrows
    public void justEnlist() {
        simulator.simulate(T_1_Q_1_G_8,
                TournamentScenario.begin()
                        .ignoreUnexpectedGames()
                        .name("justEnlist")
                        .category(c1, p1, p2, p3, p4)
                        .doNotBegin()
                        .presence(EnlistMode.Enlist, p1, p2, p3, p4));
    }

    @Test
    @SneakyThrows
    public void justEnlist2Categories3To1() {
        simulator.simulate(T_1_Q_1_G_8,
                TournamentScenario.begin()
                        .ignoreUnexpectedGames()
                        .name("justEnlist2Categories3To1")
                        .category(c1, p1, p2, p3)
                        .category(c2, p4)
                        .doNotBegin()
                        .presence(EnlistMode.Enlist, p4)
                        .presence(EnlistMode.Pass, p1, p2, p3));
    }

    @Test
    @SneakyThrows
    public void schedule3ConcurrentGames() {
        simulator.simulate(T_3_Q_1_G_8,
                TournamentScenario.begin()
                        .ignoreUnexpectedGames()
                        .name("sched 3 con games")
                        .category(c1, p1, p2, p3, p4, p5, p6));
    }

    @Test
    @SneakyThrows
    public void justBeginTournamentOf2() {
        simulator.simulate(T_1_Q_1_G_8,
                TournamentScenario.begin()
                        .ignoreUnexpectedGames()
                        .name("justBeginTournamentOf2")
                        .category(c1, p1, p2));
    }
}
