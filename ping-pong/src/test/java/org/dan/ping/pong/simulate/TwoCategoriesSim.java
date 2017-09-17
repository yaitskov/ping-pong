package org.dan.ping.pong.simulate;

import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G2Q1_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.Player.p7;
import static org.dan.ping.pong.mock.simulator.Player.p8;
import static org.dan.ping.pong.mock.simulator.Player.p9;
import static org.dan.ping.pong.mock.simulator.Player.pa;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c2;

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
        simulator.simulate(
                TournamentScenario.begin()
                        .rules(RULES_G8Q1_S1A2G11)
                        .ignoreUnexpectedGames()
                        .name("enlist3To1GrpAnd1ToAnother")
                        .category(c1, p1, p2, p3)
                        .category(c2, p4)
                        .doNotBegin()
                        .presence(EnlistMode.Enlist, p4)
                        .presence(EnlistMode.Pass, p1, p2, p3));
    }

    @Test
    public void almostComplete() {
        simulator.simulate(TournamentScenario.begin()
                .name("almostComplete")
                .rules(RULES_G2Q1_S1A2G11)
                .ignoreUnexpectedGames()
                .category(c1, p1, p2)
                .category(c2, p3, p4, p5, p6, p7, p8, p9, pa)
                .win(p3, p4)
                .win(p5, p6)
                .win(p7, p8)
                .win(p9, pa)
                .quitsGroup(p3, p5, p7, p9)
                .win(p3, p5)
                .win(p7, p9));
    }
}
