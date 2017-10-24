package org.dan.ping.pong.simulate;

import static org.dan.ping.pong.app.tournament.Kw04FirstTournamentJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;

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
public class OneCategoryNoTablesSim {
    @Inject
    private Simulator simulator;

    @Test
    @SneakyThrows
    public void justBeginTournamentOf2() {
        simulator.simulate(
                TournamentScenario.begin()
                        .tables(0)
                        .rules(RULES_G8Q1_S1A2G11)
                        .ignoreUnexpectedGames()
                        .name("justBeginTourOf2")
                        .category(c1, p1, p2));
    }
}
