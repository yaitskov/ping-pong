package org.dan.ping.pong.app.group;

import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G_S1A2G11_NP;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BALANCE_BASED_DM_ORDER_RULES;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class DisambiguationMatchJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void genDisambiguationMatchesSameMatchRule() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("genDisambiguationMatches")
                .rules(RULES_G_S1A2G11_NP.withGroup(
                        RULES_G_S1A2G11_NP.getGroup()
                                .map(g -> g.withOrderRules(BALANCE_BASED_DM_ORDER_RULES))))
                .category(c1, p1, p2, p3);

        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> {
            c.beginTournament()
                    .scoreSet(p1, 11, p2, 0)
                    .scoreSet(p2, 11, p3, 0)
                    .scoreSet(p3, 11, p1, 0)
                    .checkMatchStatus(p1, p2, Over)
                    .checkMatchStatus(p1, p3, Over)
                    .checkMatchStatus(p3, p2, Over)
                    .checkTournament(Open, restState(Play))
                    .reloadMatchMap()
                    .scoreSet(p1, 11, p2, 0)
                    .scoreSet(p3, 11, p2, 0)
                    .scoreSet(p3, 11, p1, 0)
                    .checkResult(p3, p1, p2)
                    .checkTournamentComplete(restState(Lost).bid(p3, Win1));
        });
    }
}
