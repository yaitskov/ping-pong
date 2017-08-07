package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.mock.simulator.HookDecision.Skip;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_1_G_8;
import static org.junit.Assert.assertEquals;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.mock.simulator.Hook;
import org.dan.ping.pong.mock.simulator.HookDecision;
import org.dan.ping.pong.mock.simulator.PlayHook;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentResignJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Inject
    private TournamentService tournamentService;

    @Inject
    private BidDao bidDao;

    @Test
    public void resignInDraft() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .doNotBegin()
                .name("resignInDraft")
                .category(c1, p1, p2, p3);

        simulator.simulate(T_1_Q_1_G_8, scenario);
        final int uid = scenario.getPlayersSessions().get(p1).getUid();
        tournamentService.leaveTournament(uid, scenario.getTid(), Quit);

        assertEquals(Optional.of(Quit), bidDao.getState(scenario.getTid(), uid));
    }

    @Inject
    private MatchDao matchDao;

    @Test
    public void resignInGroupMiddle() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .ignoreUnexpectedGames()
                .name("resignInGroupMiddle")
                .category(c1, p1, p2, p3)
                .w30(p1, p2)
                .pause(p1, p2, PlayHook.builder()
                        .type(Hook.BeforeScore)
                        .callback((s, players) -> {
                            final int uid = s.getPlayersSessions().get(p1).getUid();
                            tournamentService.leaveTournament(uid, s.getTid(), Quit);
                            assertEquals(Optional.of(Quit), bidDao.getState(s.getTid(), uid));

                            // assertEquals(Optional.of(Quit), matchDao.getById(s.getTid(), uid));
                            return Skip;
                        })
                        .build());

        simulator.simulate(T_1_Q_1_G_8, scenario);

    }
}
