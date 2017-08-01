package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c2;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_2_G_8;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentJerseyResultTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Inject
    private TournamentDao tournamentDao;

    @Test
    public void tournamentResult() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("tournamentResult")
                .category(c1, p1, p2, p3)
                .category(c2, p4, p5)
                .w31(p1, p2)
                .w30(p1, p3)
                .w32(p2, p3)
                .quitsGroup(p1, p2)
                .w31(p1, p2)
                .champions(c1, p1, p2)
                .w31(p4, p5)
                .quitsGroup(p4, p5)
                .w32(p4, p5)
                .champions(c2, p4, p5);

        simulator.simulate(T_1_Q_2_G_8, scenario);
//        final List<TournamentResultEntry> result = tournamentDao
//                .tournamentResult(scenario.getTid(),
//                        scenario.getCategoryDbId().get(c1));
//        TournamentResultEntry p1Result = result.stream()
//                .filter(r -> r.getUser().getUid() == scenario.getPlayersSessions().get(p1).getUid())
//                .findFirst().get();
//        assertEquals(3, p1Result.getScore());
//        assertEquals(3, p1Result.getScore());
//
//        TournamentResultEntry p2Result = result.stream()
//                .filter(r -> r.getUser().getUid() == scenario.getPlayersSessions().get(p2).getUid())
//                .findFirst().get();

    }
}
