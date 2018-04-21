package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G2Q1_S1A2G11;
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
import org.dan.ping.pong.app.tournament.PlayOffRulesConst;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class MatchPlayOffCustomMatchRulesJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void tennisPlayOff2SetsByGroupJust1() {
        isf.create(begin().name("tennisG1SetPlayOff2Sets")
                .rules(RULES_G2Q1_S1A2G11.withPlayOff(Optional.of(PlayOffRulesConst.L1_S3A2G11)))
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p2, 3)
                        .scoreSet(p3, 11, p4, 7)
                        .reloadMatchMap()
                        .scoreSet3(p1, 11, p3, 4)
                        .checkMatchStatus(p1, p3, Over)
                        .checkResult(p1, p3, p4, p2)
                        .checkTournamentComplete(restState(Lost).bid(p3, Win2).bid(p1, Win1)));
    }
}
