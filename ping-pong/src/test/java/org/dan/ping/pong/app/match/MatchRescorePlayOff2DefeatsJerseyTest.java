package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_JP2_S3A2G11;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
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
public class MatchRescorePlayOff2DefeatsJerseyTest extends AbstractSpringJerseyTest {
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
    public void rescoreBaseMtchInJp2Of2Keep() {
        isf.create(begin().name("rescoreBaseMtchInJp2Of2Keep")
                .rules(RULES_JP2_S3A2G11)
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p2, 1)
                        .storeMatchMap(firstMatch)
                        .reloadMatchMap()
                        .storeMatchMap(secondMatch)
                        .scoreSet3(p1, 3, p2, 11)
                        .restoreMatchMap(firstMatch)
                        .rescoreMatch3(p1, p2, 11, 8)
                        .checkMatchStatus(p1, p2, Over)
                        .restoreMatchMap(secondMatch)
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(restState(Win2).bid(p2, Win1)));
    }

    @Test
    public void rescoreBaseMtchInJp2Of2Swap() {
        isf.create(begin().name("rescoreBaseMtchInJp2Of2Swap")
                .rules(RULES_JP2_S3A2G11)
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p2, 1)
                        .storeMatchMap(firstMatch)
                        .reloadMatchMap()
                        .storeMatchMap(secondMatch)
                        .scoreSet3(p1, 3, p2, 11)
                        .restoreMatchMap(firstMatch)
                        .rescoreMatch3(p1, p2, 8, 11)
                        .checkMatchStatus(p1, p2, Over)
                        .restoreMatchMap(secondMatch)
                        .checkMatchStatus(p1, p2, Game)
                        .scoreSet3(p1, 11, p2, 4)
                        .checkResult(p1, p2)
                        .checkTournamentComplete(restState(Win2).bid(p1, Win1)));
    }
}
