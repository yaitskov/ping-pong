package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G_S1A2G11_NP;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c2;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class Tournament2CategoriesJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void p1ParticipatesIn2Groups() {
        isf.create(begin().name("p1ParticipatesIn2Groups")
                .rules(RULES_G_S1A2G11_NP)
                .category(c1, p1, p2)
                .category(c2, p1, p3))
                .run(c -> c.beginTournament()
                        .scoreSet(c1, p1, 11, p2, 0)
                        .scoreSet(c2, p3, 11, p1, 0)
                        .checkTournamentComplete(c1,
                                BidStatesDesc.restState(Expl)
                                        .bid(p1, Win1)
                                        .bid(p2, Lost))
                        .checkTournamentComplete(c2,
                                BidStatesDesc.restState(Expl)
                                        .bid(p3, Win1)
                                        .bid(p1, Lost))
                        .checkResult(c1, p1, p2)
                        .checkResult(c2, p3, p1));
    }
}
