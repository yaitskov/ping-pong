package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.sport.SportType.Tennis;
import static org.dan.ping.pong.app.sport.tennis.TennisSportTest.CLASSIC_TENNIS_RULES;
import static org.dan.ping.pong.app.tournament.AtpTennisTournamentJerseyTest.T_RULES_G8_S2A2G6;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TennisTournamentJerseyTest extends AbstractSpringJerseyTest {
    private static final TournamentRules RULES = T_RULES_G8_S2A2G6
            .withMatch(CLASSIC_TENNIS_RULES
                    .withSuperTieBreakGames(Optional.empty()))
            .withPlayOff(Optional.empty());
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void normalSetInsteadOfTieBreak() {
        isf.create(begin()
                .name("normalSetInsteadOfTieBreak")
                .sport(Tennis)
                .category(c1, p1, p2)
                .rules(RULES))
                .run(c -> c.beginTournament()
                        .scoreSet(0, p1, 6, p2, 0)
                        .scoreSet(1, p1, 0, p2, 6)
                        .scoreSet(2, p1, 0, p2, 6)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(BidStatesDesc
                                .restState(Lost)
                                .bid(p2, Win1)));
    }

    @Test
    public void normalSetInsteadOfTieBreak75() {
        isf.create(begin()
                .name("normalSetInsteadOfTieBrk75")
                .sport(Tennis)
                .category(c1, p1, p2)
                .rules(RULES))
                .run(c -> c.beginTournament()
                        .scoreSet(0, p1, 7, p2, 5)
                        .scoreSet(1, p1, 6, p2, 7)
                        .scoreSet(2, p1, 5, p2, 7)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(BidStatesDesc
                                .restState(Lost)
                                .bid(p2, Win1)));
    }

    @Test
    public void superTieBreakMore10() {
        isf.create(begin()
                .name("superTieBreakMore10")
                .sport(Tennis)
                .category(c1, p1, p2)
                .rules(RULES.withMatch(CLASSIC_TENNIS_RULES)))
                .run(c -> c.beginTournament()
                        .scoreSet(0, p1, 7, p2, 5)
                        .scoreSet(1, p1, 6, p2, 7)
                        .scoreSet(2, p1, 10, p2, 12)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(BidStatesDesc
                                .restState(Lost)
                                .bid(p2, Win1)));
    }
}
