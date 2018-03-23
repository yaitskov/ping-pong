package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.G8Q1;
import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.S3A2G11_COS;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G_S3A2G11_NP;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class MatchCountOnlySetsJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void countOnlySets() {
        isf.create(begin().name("countOnlySets")
                .rules(RULES_G_S3A2G11_NP
                        .withMatch(S3A2G11_COS)
                        .withGroup(Optional.of(G8Q1.withQuits(3))))
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet(p2, 3, p1, 2)
                        .reloadMatchMap()
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(restState(Win2).bid(p2, Win1)));
    }
}
