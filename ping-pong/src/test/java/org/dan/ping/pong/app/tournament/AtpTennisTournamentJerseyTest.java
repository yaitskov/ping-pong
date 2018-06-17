package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.group.GroupRules.ATP_BASED_ORDER_RULES;
import static org.dan.ping.pong.app.sport.SportType.Tennis;
import static org.dan.ping.pong.app.sport.tennis.TennisSportTest.CLASSIC_TENNIS_RULES;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G_S1A2G11_NP;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;

import org.dan.ping.pong.JerseySpringTest;
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
public class AtpTennisTournamentJerseyTest extends AbstractSpringJerseyTest {
    public static final TournamentRules T_RULES_G8_S2A2G6
            = RULES_G_S1A2G11_NP.withMatch(CLASSIC_TENNIS_RULES)
            .withGroup(RULES_G_S1A2G11_NP.getGroup()
                    .map(g -> g.withOrderRules(ATP_BASED_ORDER_RULES)));

    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void groupOf4ApplyRuleD1() {
        isf.create(makeScenario("groupOf4ApplyRuleD1"))
                .run(c -> c.beginTournament()
                        .scoreSet2(p1, 6, p2, 0)
                        .scoreSet2(p1, 0, p3, 6)
                        .scoreSet2(p1, 0, p4, 6)

                        .scoreSet2(p2, 6, p3, 0)
                        .scoreSet2(p2, 0, p4, 6)

                        // p3 vs p4 left
                        .expelPlayer(p3) // p1 wins
                        .checkResult(p4, p1, p2, p3)

                        .checkTournamentComplete(BidStatesDesc
                                .restState(Lost)
                                .bid(p4, Win1)
                                .bid(p3, Expl)));
    }

    @Test
    public void groupOf4ApplyRuleD2() {
        isf.create(makeScenario("groupOf4ApplyRuleD2"))
                .run(c -> c.beginTournament()
                        .scoreSet2(p1, 0, p2, 6)
                        .scoreSet(p1, 6, p3, 1)
                        .scoreSet(1, p1, 0, p3, 6)
                        .scoreSet(2, p1, 0, p3, 10)
                        .scoreSet2(p1, 0, p4, 6)

                        .scoreSet2(p2, 6, p3, 0)
                        .scoreSet2(p2, 0, p4, 6)
                        .scoreSet2(p3, 6, p4, 0)
                        .checkResult(p4, p2, p3, p1)
                        .checkTournamentComplete(BidStatesDesc
                                .restState(Lost)
                                .bid(p4, Win1)));
    }

    @Test
    public void groupOf4ApplyRuleD3() {
        isf.create(makeScenario("groupOf4ApplyRuleD3"))
                .run(c -> c.beginTournament()
                        .scoreSet2(p1, 0, p2, 6)
                        .scoreSet2(p1, 0, p3, 6)
                        .scoreSet2(p1, 4, p4, 6)

                        .scoreSet2(p2, 6, p3, 1)
                        .scoreSet2(p2, 3, p4, 6)
                        .scoreSet2(p3, 6, p4, 2)
                        .checkResult(p2, p3, p4, p1)
                        .checkTournamentComplete(BidStatesDesc
                                .restState(Lost)
                                .bid(p2, Win1)));
    }

    private TournamentScenario makeScenario(String name) {
        return begin()
                .name(name)
                .sport(Tennis)
                .category(c1, p1, p2, p3, p4)
                .rules(T_RULES_G8_S2A2G6);
    }
}
