package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.S1A2G11;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing1;
import static org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin.INCREASE_SIGNUP_CASTING;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
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
public class JustPlayOffJerseyTest extends AbstractSpringJerseyTest {
    private static final TournamentRules RULES_PO_S1A2G11 = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.empty())
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .build();

    @Inject
    private Simulator simulator;

    @Test
    public void noAutoLoosers4() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2, p3, p4)
                .quitsGroup(p1, p2, p3, p4)
                .win(p1, p4)
                .win(p2, p3)
                .win(p1, p2)
                .champions(c1, p1, p2)
                .name("noAutoLoosers4");

        simulator.simulate(scenario);
    }

    @Test
    public void oneAutoLoosers3() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2, p3)
                .quitsGroup(p1, p2, p3)
                .win(p2, p3)
                .win(p1, p2)
                .champions(c1, p1, p2)
                .name("oneAutoLoosers3");

        simulator.simulate(scenario);
    }

    @Test
    public void threeAutoLoosers5() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2, p3, p4, p5)
                .quitsGroup(p1, p2, p3, p4, p5)
                .win(p5, p4)
                .win(p3, p2)
                .win(p1, p5)
                .win(p3, p1)
                .champions(c1, p3, p1)
                .name("3AutoLoosers5");

        simulator.simulate(scenario);
    }
}
