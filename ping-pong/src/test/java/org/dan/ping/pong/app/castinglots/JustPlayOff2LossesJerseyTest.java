package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.S1A2G11;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing2;
import static org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin.INCREASE_SIGNUP_CASTING;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.Player.p7;
import static org.dan.ping.pong.mock.simulator.Player.p8;
import static org.dan.ping.pong.mock.simulator.Player.p9;
import static org.dan.ping.pong.mock.simulator.Player.p10;
import static org.dan.ping.pong.mock.simulator.Player.p11;
import static org.dan.ping.pong.mock.simulator.Player.p12;
import static org.dan.ping.pong.mock.simulator.Player.p13;
import static org.dan.ping.pong.mock.simulator.Player.p14;
import static org.dan.ping.pong.mock.simulator.Player.p15;
import static org.dan.ping.pong.mock.simulator.Player.p16;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class JustPlayOff2LossesJerseyTest extends AbstractSpringJerseyTest {
    private static final TournamentRules RULES_PO_S1A2G11 = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.empty())
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing2))
            .build();

    @Inject
    private Simulator simulator;

    @Test
    public void noAutoLoosers2() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2)
                .quitsGroup(p1, p2)
                .win(p1, p2)
                .win(p2, p1)
                .champions(c1, p2, p1)
                .name("noAutoLoosers2");

        simulator.simulate(scenario);
    }

    @Test
    public void oneAutoLooser4() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2, p3)
                .quitsGroup(p1, p2, p3)
                .win(p2, p3)
                .win(p1, p2)
                .win(p3, p2)
                .win(p1, p3)
                .champions(c1, p1, p3, p2)
                .name("oneAutoLooser4");

        simulator.simulate(scenario);
    }

    @Test
    public void noAutoLoosers4() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2, p3, p4)
                .quitsGroup(p1, p2, p3, p4)
                .win(p1, p4)
                .win(p2, p3)
                .win(p1, p2)
                .win(p3, p4)
                .win(p3, p2)
                .win(p1, p3)
                .champions(c1, p1, p3, p2)
                .name("noAutoLoosers4");

        simulator.simulate(scenario);
    }

    @Test
    public void noAutoLoosers8() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8)
                .quitsGroup(p1, p2, p3, p4, p5, p6, p7, p8)
                // first round
                .win(p1, p8)
                .win(p4, p5)
                .win(p3, p6)
                .win(p2, p7)
                // second round
                .win(p1, p4)
                .win(p2, p3)
                // third round
                .win(p1, p2)
                // losers
                .win(p5, p8)
                .win(p6, p7)
                //
                .win(p3, p5)
                .win(p4, p6)
                .win(p3, p4)
                // loser get third place
                .win(p3, p2)
                // final
                .win(p3, p1)
                .champions(c1, p3, p1, p2)
                .name("noAutoLoosers8");

        simulator.simulate(scenario);
    }

    @Test
    public void noAutoLoosers16() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8,
                        p9, p10, p11, p12, p13, p14, p15, p16)
                .quitsGroup(p1, p1, p2, p3, p4, p5, p6, p7, p8,
                        p9, p10, p11, p12, p13, p14, p15, p16)
                // first round
                .win(p1, p16)
                .win(p8, p9)
                .win(p5, p12)
                .win(p4, p13)
                //
                .win(p3, p14)
                .win(p6, p11)
                .win(p7, p10)
                .win(p2, p15)
                // second round
                .win(p1, p8)
                .win(p4, p5)
                .win(p3, p6)
                .win(p2, p7)
                // third round
                .win(p1, p4)
                .win(p2, p3)
                // forth round
                .win(p1, p2)
                // losers
                .win(p9, p16)
                .win(p12, p13)
                .win(p11, p14)
                .win(p10, p15)
                //
                .win(p7, p9)
                .win(p6, p12)
                .win(p5, p11)
                .win(p8, p10)
                //
                .win(p6, p7)
                .win(p5, p8)
                //
                .win(p3, p6)
                .win(p4, p5)
                //
                .win(p3, p4)
                //
                .win(p2, p3)
                // final
                .win(p2, p1)
                .champions(c1, p2, p1, p3)
                .name("noAutoLoosers16");

        simulator.simulate(scenario);
    }
}
