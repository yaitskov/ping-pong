package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11_NP;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.simulator.EnlistMode;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class SmallTournamentAutoCompleteJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void autoCloseWithoutActiveParticipants() {
        final TournamentScenario scenario = begin().name("autoCloseWhen0Players")
                .rules(RULES_G8Q1_S1A2G11_NP)
                .category(c1, p1)
                .presence(EnlistMode.Quit, p1);

        isf.create(scenario)
                .run(c -> c.beginTournament()
                        .checkResult()
                        .checkTournamentComplete(BidStatesDesc.restState(Quit)));
    }

    @Test
    public void autoCloseWith1ActiveParticipants() {
        final TournamentScenario scenario = begin().name("autoCloseWhen1Player")
                .rules(RULES_G8Q1_S1A2G11_NP)
                .category(c1, p1);

        isf.create(scenario)
                .run(c -> c.beginTournament()
                        .checkResult(p1)
                        .checkTournamentComplete(BidStatesDesc.restState(Win1)));
    }
}
