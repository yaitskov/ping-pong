package org.dan.ping.pong.app.server.castinglots;

import static org.dan.ping.pong.app.server.castinglots.MatchScheduleInGroupJerseyTest.G8Q1;
import static org.dan.ping.pong.app.server.castinglots.MatchScheduleInGroupJerseyTest.G8Q2;
import static org.dan.ping.pong.app.server.castinglots.MatchScheduleInGroupJerseyTest.S1A2G11;
import static org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin.INCREASE_SIGNUP_CASTING;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.server.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.server.tournament.TournamentRules;
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
public class MiniTournamentJerseyTest extends AbstractSpringJerseyTest {
    public static final TournamentRules RULES_G8Q2_S1A2G11_MINI = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G8Q2))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.empty())
            .build();
    public static final TournamentRules RULES_G8Q1_S1A2G11_MINI = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G8Q1))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.empty())
            .build();

    @Inject
    private Simulator simulator;

    @Test
    public void groupOf2Quits1() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_G8Q1_S1A2G11_MINI)
                .category(c1, p1, p2)
                .win(p2, p1)
                .quitsGroup(p2)
                .champions(c1, p2)
                .name("groupOf2Quits1");
        simulator.simulate(scenario);
    }

//    @Test
//    public void groupOf2Quits3() {
//        final TournamentScenario scenario = TournamentScenario.begin()
//                .rules(RULES_G8Q3_S1A2G11_MINI)
//                .category(c1, p1, p2)
//                .win(p2, p1)
//                .quitsGroup(p2, p1)
//                .champions(c1, p2, p1)
//                .name("groupOf2Quits3");
//        simulator.simulate(scenario);
//    }

    @Test
    public void groupOf4Quits2() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_G8Q2_S1A2G11_MINI)
                .category(c1, p1, p2, p3, p4)
                .win(p4, p1)
                .win(p2, p1)
                .win(p2, p3)
                .win(p2, p4)
                .win(p3, p1)
                .win(p3, p4)
                .quitsGroup(p2, p3)
                .champions(c1, p2, p3)
                .name("groupOf4Quits2");
        simulator.simulate(scenario);
    }
}
