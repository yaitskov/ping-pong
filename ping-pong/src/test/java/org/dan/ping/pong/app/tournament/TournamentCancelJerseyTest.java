package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q2_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentResource.CANCEL_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentState.Canceled;
import static org.dan.ping.pong.mock.simulator.HookDecision.Skip;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.HashMultimap;
import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.simulator.Hook;
import org.dan.ping.pong.mock.simulator.PlayHook;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentCancelJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void cancelRunningTournament() {
        final TournamentScenario scenario = TournamentScenario
                .begin()
                .terminalState(Canceled)
                .rules(RULES_G8Q2_S1A2G11)
                .category(c1, p1, p2, p3)
                .win(p1, p3)
                .pause(p1, p3, PlayHook.builder()
                        .type(Hook.AfterMatch)
                        .callback((s, m) -> {
                            assertEquals(s.getGroupMatches(), HashMultimap.create());
                            s.getGroupMatches().clear();
                            myRest().voidPost(CANCEL_TOURNAMENT, s.getTestAdmin(), s.getTid());
                            return Skip;
                        })
                        .build())
                .name("cancelRunningTournament");
        simulator.simulate(scenario);
    }
}
