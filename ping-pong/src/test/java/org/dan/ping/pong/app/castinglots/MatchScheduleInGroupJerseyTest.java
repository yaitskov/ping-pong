package org.dan.ping.pong.app.castinglots;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing1;
import static org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin.INCREASE_SIGNUP_CASTING;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.group.GroupSchedule;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.tournament.MatchValidationRule;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.mock.simulator.Player;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class MatchScheduleInGroupJerseyTest extends AbstractSpringJerseyTest {
    public static final GroupRules G8Q1 = GroupRules.builder()
            .groupSize(8)
            .quits(1)
            .build();

    public static final GroupRules G8Q2 = GroupRules.builder()
            .groupSize(8)
            .quits(2)
            .build();

    public static final GroupRules G2Q1 = GroupRules.builder()
            .groupSize(2)
            .quits(1)
            .build();

    public static final MatchValidationRule S1A2G11 = MatchValidationRule.builder()
            .setsToWin(1)
            .minAdvanceInGames(2)
            .minPossibleGames(0)
            .minGamesToWin(11)
            .build();

    public static final MatchValidationRule S3A2G11 = MatchValidationRule.builder()
            .setsToWin(3)
            .minAdvanceInGames(2)
            .minPossibleGames(0)
            .minGamesToWin(11)
            .build();

    @Inject
    private Simulator simulator;

    @Inject
    private TournamentService tournamentService;

    @Test
    public void groupOf3CustomSchedule() {
        final List<Set<Player>> matchOrder = new ArrayList<>();
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("groupOf3CustomSchedule")
                .category(c1, p1, p2, p3)
                .rules(TournamentRules.builder()
                        .group(Optional.of(G8Q1.withSchedule(Optional.of(
                                GroupSchedule.builder()
                                        .size2Schedule(
                                                ImmutableMap.of(3, asList(2, 1, 1, 0, 2, 0)))
                                        .build()))))
                        .casting(INCREASE_SIGNUP_CASTING)
                        .playOff(Optional.of(Losing1))
                        .match(S1A2G11)
                        .build())
                .onBeforeMatch((s, minfo) -> matchOrder.add(minfo.getPlayers()))
                .w10(p1, p2)
                .w10(p1, p3)
                .w10(p2, p3)
                .quitsGroup(p1)
                .champions(c1, p1);

        simulator.simulate(scenario);

        assertThat(matchOrder, is(asList(
                ImmutableSet.of(p3, p2),
                ImmutableSet.of(p1, p2),
                ImmutableSet.of(p3, p1))));
    }

    @Test
    public void groupOf3DefaultSchedule() {
        final List<Set<Player>> matchOrder = new ArrayList<>();
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("groupOf3DefaultSchedule")
                .category(c1, p1, p2, p3)
                .rules(TournamentRules.builder()
                        .group(Optional.of(G8Q1))
                        .playOff(Optional.of(Losing1))
                        .match(S1A2G11)
                        .casting(INCREASE_SIGNUP_CASTING)
                        .build())
                .onBeforeMatch((s, minfo) -> matchOrder.add(minfo.getPlayers()))
                .w10(p1, p2)
                .w10(p1, p3)
                .w10(p2, p3)
                .quitsGroup(p1)
                .champions(c1, p1);

        simulator.simulate(scenario);

        assertThat(matchOrder, is(asList(
                ImmutableSet.of(p1, p3),
                ImmutableSet.of(p1, p2),
                ImmutableSet.of(p2, p3))));
    }
}
