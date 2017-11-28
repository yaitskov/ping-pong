package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G2Q1_S1A2G11;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.Player;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class MatchRescoreGroupAndPlayOffJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    final Map<Set<Player>, Mid> firstMatch = new HashMap<>();
    final Map<Set<Player>, Mid> secondMatch = new HashMap<>();

    @After
    public void cleanMaps() {
        firstMatch.clear();
        secondMatch.clear();
    }

    @Test
    public void rescoreMatchInGroup2Of2Subtle() {
        isf.create(begin().name("rescoreMatchInGroup2Of2Subtle")
                .rules(RULES_G2Q1_S1A2G11)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p2, 1)
                        .scoreSet(p3, 11, p4, 2)
                        .scoreSet(p1, 11, p3, 3)
                        .checkResult(p1, p3, p4, p2)
                        .checkTournamentComplete(restState(Lost).bid(p1, Win1).bid(p3, Win2))
                        .rescoreMatch(p1, p2, 11, 9)
                        .checkMatchStatus(p1, p2, Over)
                        .checkMatchStatus(p1, p3, Over)
                        .checkResult(p1, p3, p2, p4)
                        .checkTournamentComplete(restState(Lost).bid(p1, Win1).bid(p3, Win2)));
    }
}
