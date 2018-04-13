package org.dan.ping.pong.app.castinglots;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.group.GroupRules.BALANCE_BASED_ORDER_RULES;
import static org.dan.ping.pong.app.group.GroupRules.WON_LOST_BASED_ORDER_RULES;
import static org.dan.ping.pong.app.match.MatchJerseyTest.GLOBAL;
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
import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
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
            .orderRules(WON_LOST_BASED_ORDER_RULES)
            .build();

    public static final GroupRules G8Q2 = G8Q1.withQuits(2);
    public static final GroupRules G3Q2 = G8Q2.withGroupSize(3);
    public static final GroupRules G3Q1 = G3Q2.withQuits(1);
    public static final GroupRules G2Q1 =  G3Q1.withGroupSize(2);

    public static final GroupRules G8Q2_M = G8Q2.withOrderRules(BALANCE_BASED_ORDER_RULES);

    public static final PingPongMatchRules S1A2G11 = PingPongMatchRules.builder()
            .setsToWin(1)
            .minAdvanceInGames(2)
            .minPossibleGames(0)
            .minGamesToWin(11)
            .build();

    public static final PingPongMatchRules S1 = S1A2G11.withCountOnlySets(true);

    public static final PingPongMatchRules S3A2G11 = PingPongMatchRules.builder()
            .setsToWin(3)
            .minAdvanceInGames(2)
            .minPossibleGames(0)
            .minGamesToWin(11)
            .build();

    public static final PingPongMatchRules S3A2G11_COS = S3A2G11.withCountOnlySets(true);

    public static final PingPongMatchRules S3 = S3A2G11.withCountOnlySets(true);

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
                        .place(Optional.of(GLOBAL))
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
                        .place(Optional.of(GLOBAL))
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
