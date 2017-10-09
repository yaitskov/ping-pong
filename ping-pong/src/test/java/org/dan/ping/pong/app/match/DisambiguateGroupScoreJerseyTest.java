package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.S1A2G11;
import static org.dan.ping.pong.app.group.DisambiguationPolicy.CMP_WIN_MINUS_LOSE;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q2_S1A2G11;
import static org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin.INCREASE_SIGNUP_CASTING;
import static org.dan.ping.pong.mock.simulator.FixedSetGenerator.game;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.group.GroupRules;
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
public class DisambiguateGroupScoreJerseyTest extends AbstractSpringJerseyTest {
    public static final GroupRules G8Q2_MINUS = GroupRules.builder()
            .groupSize(8)
            .quits(2)
            .disambiguation(CMP_WIN_MINUS_LOSE)
            .build();

    public static final TournamentRules RULES_G8Q2_S1A2G11_M = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G8Q2_MINUS))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.empty())
            .build();

    @Inject
    private Simulator simulator;

    @Test
    public void winMinusLose() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("winMinusLose")
                .rules(RULES_G8Q2_S1A2G11_M)
                .category(c1, p1, p2, p3)
                .custom(game(p1, p2, 11, 0))
                .custom(game(p2, p3, 11, 1))
                .custom(game(p3, p1, 14, 12))
                .quitsGroup(p1, p2)
                .champions(c1, p1, p2);

        simulator.simulate(scenario);
    }

    @Test
    public void compareWinAndLose() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("cmpWinAndLose")
                .rules(RULES_G8Q2_S1A2G11.withPlayOff(Optional.empty()))
                .category(c1, p1, p2, p3)
                .custom(game(p1, p2, 11, 0))
                .custom(game(p2, p3, 11, 1))
                .custom(game(p3, p1, 14, 12))
                .quitsGroup(p3, p1)
                .champions(c1, p1, p3);

        simulator.simulate(scenario);
    }
}
