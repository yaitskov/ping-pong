package org.dan.ping.pong.simulate;

import static org.dan.ping.pong.app.server.match.MatchJerseyTest.RULES_G2Q1_S1A2G11;
import static org.dan.ping.pong.app.server.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.server.match.MatchJerseyTest.RULES_G8Q1_S3A2G11;
import static org.dan.ping.pong.mock.simulator.AutoResolution.RANDOM;
import static org.dan.ping.pong.mock.simulator.FixedSetGenerator.game;
import static org.dan.ping.pong.mock.simulator.Hook.BeforeScore;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;

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
public class OneCategorySim {
    @Inject
    private Simulator simulator;

    @Test
    @SneakyThrows
    public void pauseBeforeSecondScore() {
        simulator.simulate(
                TournamentScenario.begin()
                        .rules(RULES_G8Q1_S3A2G11)
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
        simulator.simulate(
                TournamentScenario.begin()
                        .rules(RULES_G8Q1_S3A2G11)
                        .ignoreUnexpectedGames()
                        .name("justEnlist")
                        .category(c1, p1, p2, p3, p4)
                        .doNotBegin()
                        .presence(EnlistMode.Enlist, p1, p2, p3, p4));
    }


    @Test
    @SneakyThrows
    public void schedule3ConcurrentGames() {
        simulator.simulate(
                TournamentScenario.begin()
                        .tables(3)
                        .rules(RULES_G8Q1_S3A2G11)
                        .ignoreUnexpectedGames()
                        .name("sched 3 con games")
                        .category(c1, p1, p2, p3, p4, p5, p6));
    }

    @Test
    @SneakyThrows
    public void justBeginTournamentOf2() {
        simulator.simulate(
                TournamentScenario.begin()
                        .rules(RULES_G8Q1_S3A2G11)
                        .ignoreUnexpectedGames()
                        .name("justBeginTournamentOf2")
                        .category(c1, p1, p2));
    }

    @Test
    public void quits2Of3() {
        simulator.simulate(TournamentScenario.begin()
                .name("quits2Of3")
                .rules(RULES_G8Q1_S3A2G11)
                .category(c1, p1, p2, p3)
                .w31(p1, p2)
                .w30(p1, p3)
                .w32(p2, p3)
                .quitsGroup(p1, p2)
                .w31(p1, p2)
                .champions(c1, p1, p2));
    }

    @Test(expected = AssertionError.class)
    public void quits2Of2() {
        simulator.simulate(TournamentScenario.begin()
                .ignoreUnexpectedGames()
                .rules(RULES_G8Q1_S1A2G11)
                .name("quits2Of2")
                .category(c1, p1, p2));
    }

    @Test
    public void almostComplete() {
        simulator.simulate(TournamentScenario.begin()
                .name("almostComplete")
                .rules(RULES_G2Q1_S1A2G11)
                .ignoreUnexpectedGames()
                .category(c1, p1, p2, p3, p4)
                .win(p1, p2)
                .win(p3, p4)
                .quitsGroup(p1, p3)
                .win(p1, p3)
                .pause(p1, p3, BeforeScore)
                .champions(c1, p1, p3));
    }

    @Test
    public void scoreLastSet() {
        simulator.simulate(TournamentScenario.begin()
                .name("scoreLastSet")
                .rules(RULES_G8Q1_S3A2G11)
                .ignoreUnexpectedGames()
                .category(c1, p1, p2)
                .custom(game(p1, p2, 11, 0, 11, 1, 0, 0, 11, 2))
                .quitsGroup(p1)
                .champions(c1, p1));
    }
}
