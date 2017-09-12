package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.tournament.TournamentResource.RESULT_CATEGORY;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RESULT;
import static org.dan.ping.pong.mock.simulator.FixedSetGenerator.game;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_1_G_8;
import static org.junit.Assert.assertEquals;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.tournament.TournamentResultEntry;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class MatchResetJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Inject
    private TournamentService tournamentService;

    @Test
    public void resetOpenGroupMatch() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("resetOpenGroupMatch")
                .category(c1, p1, p2)
                .custom(game(p1, p2, 0, 11, 1, 11)
                        .reset(0, myRest())
                        .set(11, 4).set(11, 5).set(11, 6))
                .quitsGroup(p1)
                .champions(c1, p1);

        simulator.simulate(T_1_Q_1_G_8, scenario);

        final List<TournamentResultEntry> result = myRest().get(
                TOURNAMENT_RESULT + scenario.getTid() + RESULT_CATEGORY + scenario.getCategoryDbId().get(c1),
                scenario.getTestAdmin(),
                new GenericType<List<TournamentResultEntry>>() {});

        assertEquals(result.get(0).getUser().getUid(), scenario.getPlayersSessions().get(p1).getUid());
        assertEquals(result.get(0).getScore().getRating().getLostSets(), 0);
        assertEquals(result.get(0).getScore().getRating().getWinSets(), 3);
        assertEquals(result.get(1).getUser().getUid(), scenario.getPlayersSessions().get(p2).getUid());
        assertEquals(result.get(1).getScore().getRating().getLostSets(), 3);
        assertEquals(result.get(1).getScore().getRating().getWinSets(), 0);
    }
}
