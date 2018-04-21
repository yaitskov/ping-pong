package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S3A2G11;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
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

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class MatchRescoreStateJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void rescoreGroupMatchInStatePlace() {
        isf.create(begin().name("rescoreGroupMatchInPlaceSt")
                .rules(RULES_G8Q1_S3A2G11)
                .category(c1, p1, p2, p3))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p3, 0)
                        .rescoreMatch(p1, p3, 2, 11)
                        .checkMatchStatus(p1, p3, Place)
                        .rescoreMatch3(p1, p3, 3, 11)
                        .scoreSet3(p1, 11, p2, 1)
                        .scoreSet3(p2, 2, p3, 11)
                        .checkResult(p3, p1, p2)
                        .checkTournamentComplete(restState(Lost).bid(p3, Win1)));
    }
}
