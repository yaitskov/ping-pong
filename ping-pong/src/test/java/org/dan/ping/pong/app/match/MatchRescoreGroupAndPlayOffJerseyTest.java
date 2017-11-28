package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G2Q1_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G3Q2_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q2_S1A2G11;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
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

    final Map<Set<Player>, Mid> groupMatches = new HashMap<>();
    final Map<Set<Player>, Mid> playOffMatches = new HashMap<>();

    @After
    public void cleanMaps() {
        groupMatches.clear();
        playOffMatches.clear();
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

    @Test
    public void rescoreMtchInGrp4ChangeAllQuittersByScore() {
        isf.create(begin().name("chngGrpAllQuittersByScore")
                .rules(RULES_G8Q2_S1A2G11)
                .category(c1, p1, p2, p3))
                .run(c -> c.beginTournament()
                        .scoreSet(p3, 11, p1, 2)
                        .scoreSet(p1, 11, p2, 1)
                        .scoreSet(p2, 11, p3, 3)
                        .storeMatchMap(groupMatches)
                        .reloadMatchMap()
                        .storeMatchMap(playOffMatches)
                        .scoreSet(p3, 8, p1, 11)
                        .checkResult(p1, p3, p2)
                        .checkTournamentComplete(restState(Lost).bid(p1, Win1).bid(p3, Win2))
                        .restoreMatchMap(groupMatches)
                        .rescoreMatch(p1, p2, 11, 9)
                        .checkMatchStatus(p1, p2, Over)
                        .reloadMatchMap()
                        .checkMatchStatus(p2, p3, Game)
                        .scoreSet(p2, 4, p3, 11)
                        .checkResult(p3, p2, p1)
                        .checkTournamentComplete(restState(Lost).bid(p3, Win1).bid(p2, Win2)));
    }

    @Test
    public void rescoreMtchInGrp6ChangeOneQuitterByScore() {
        isf.create(begin().name("chngGrpOneQuitterByScore")
                .rules(RULES_G3Q2_S1A2G11)
                .category(c1, p1, p2, p3, p4, p5, p6))
                .run(c -> c.beginTournament()
                        .scoreSet(p3, 11, p1, 2)
                        .scoreSet(p6, 11, p4, 2)

                        .scoreSet(p1, 11, p2, 1)
                        .scoreSet(p4, 11, p5, 0)

                        .scoreSet(p2, 11, p3, 3)
                        .scoreSet(p5, 11, p6, 3)
                        .storeMatchMap(groupMatches)
                        .reloadMatchMap()
                        .scoreSet(p3, 8, p4, 11)
                        .scoreSet(p1, 11, p6, 5)
                        .scoreSet(p1, 11, p4, 8)
                        .checkResult(p1, p4, p3, p6, p2, p5)
                        .storeMatchMap(playOffMatches)
                        .checkTournamentComplete(restState(Lost).bid(p1, Win1).bid(p4, Win2)));//
//                        .restoreMatchMap(groupMatches)
//                        .rescoreMatch(p1, p2, 11, 9)
//                        .checkMatchStatus(p1, p2, Over)
//                        .reloadMatchMap()
//                        .checkMatchStatus(p2, p3, Game)
//                        .scoreSet(p2, 4, p3, 11)
//                        .checkResult(p3, p2, p1)
//                        .checkTournamentComplete(restState(Lost).bid(p3, Win1).bid(p2, Win2)));
    }
}
