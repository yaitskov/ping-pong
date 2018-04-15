package org.dan.ping.pong.app.match.rule;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.GID;
import static org.dan.ping.pong.app.group.GroupServiceTest.UID2;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.BallsBalance;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.F2F;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.Punkts;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.SetsBalance;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.UseDisambiguationMatches;
import static org.dan.ping.pong.app.match.rule.rules.common.DirectOutcomeRule.DIRECT_OUTCOME_RULE;
import static org.dan.ping.pong.app.sport.SportType.PingPong;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.group.MatchListBuilder;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.rules.common.BallsBalanceRule;
import org.dan.ping.pong.app.match.rule.rules.common.PickRandomlyRule;
import org.dan.ping.pong.app.match.rule.rules.common.SetsBalanceRule;
import org.dan.ping.pong.app.match.rule.rules.meta.UseDisambiguationMatchesDirective;
import org.dan.ping.pong.app.match.rule.rules.ping.CountJustPunktsRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.sport.SportCtx;
import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {GroupOrderRuleServiceCtx.class, SportCtx.class})
public class GroupParticipantOrderServiceTest {
    public static final List<GroupOrderRule> BALANCE_BASED_DM_ORDER_RULES =
            asList(new CountJustPunktsRule(),
                    DIRECT_OUTCOME_RULE,
                    new SetsBalanceRule(),
                    DIRECT_OUTCOME_RULE,
                    new BallsBalanceRule(),
                    DIRECT_OUTCOME_RULE,
                    new UseDisambiguationMatchesDirective(),
                    new CountJustPunktsRule(),
                    DIRECT_OUTCOME_RULE,
                    new SetsBalanceRule(),
                    DIRECT_OUTCOME_RULE,
                    new BallsBalanceRule(),
                    DIRECT_OUTCOME_RULE,
                    new PickRandomlyRule());

    public static final Uid UID3 = new Uid(3);
    public static final Uid UID4 = new Uid(4);
    public static final Uid UID5 = new Uid(5);
    public static final int GID2 = 2;
    public static final Uid UID6 = new Uid(6);

    private static final PingPongMatchRules S1A2G11 = PingPongMatchRules.builder()
            .setsToWin(1)
            .minAdvanceInGames(2)
            .minPossibleGames(0)
            .minGamesToWin(11)
            .build();

    private static TournamentMemState tournamentForOrder() {
        return TournamentMemState.builder()
                .sport(PingPong)
                .rule(TournamentRules.builder()
                        .match(S1A2G11)
                        .group(Optional.of(GroupRules.builder().build()))
                        .build())
                .build();
    }

    @Inject
    private GroupParticipantOrderService sut;

    private void checkOrder(List<Uid> expectedOrder, GroupParticipantOrder got) {
        checkOrder(expectedOrder, got, emptyMap());
    }

    private void checkOrder(List<Uid> expectedOrder, GroupParticipantOrder got,
            Map<Uid, List<OrderRuleName>> reasonChains) {
        assertThat(got.getAmbiguousPositions(), is(emptySet()));
        assertThat(got.getPositions().values().stream()
                        .map(GroupPosition::getCompetingUids).collect(toList()),
                is(expectedOrder.stream().map(Collections::singleton)
                        .collect(toList())));
        got.getPositions().values()
                .forEach(gp -> {
                    final List<OrderRuleName> reasonChain = gp.reasonChain().stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(Reason::getRule)
                            .collect(toList());
                    gp.getCompetingUids().forEach(uid -> {
                        final List<OrderRuleName> expected = reasonChains.get(uid);
                        if (expected == null) {
                            return;
                        }
                        assertThat("uid " + uid, reasonChain, is(expected));
                    });
                });
    }

    private GroupRuleParams params(int gid, TournamentMemState tournament,
            MatchListBuilder matchListBuilder) {
        return GroupRuleParams.ofParams(gid, tournament,
                matchListBuilder.build(), tournament.orderRules());
    }

    @Test
    public void orderUidsInGroupDisambiguates2Participants() {
        checkOrder(asList(UID2, UID5, UID4, UID3),
                sut.findOrder(params(GID, tournamentForOrder(),
                        MatchListBuilder.matches()
                                .om(UID2, 11, UID4, 1)
                                .om(UID2, 11, UID5, 1)

                                .om(UID2, 1, UID3, 11)
                                .om(UID4, 11, UID3, 1)

                                .om(UID5,  11, UID3, 1)
                                .om(UID5,  11, UID4, 1))));
    }

    @Test
    public void orderUidsWithDisambiguateMatches() {
        final TournamentMemState tournament = tournamentForOrder();
        tournament.getRule().getGroup().get()
                .setOrderRules(BALANCE_BASED_DM_ORDER_RULES);
        checkOrder(asList(UID2, UID4, UID3),
                sut.findOrder(params(GID, tournament,
                        MatchListBuilder.matches()
                                .om(UID2, 11, UID4, 1)
                                .om(UID2, 1, UID3, 11)
                                .om(UID4, 11, UID3, 1)

                                .dm(UID2, 11, UID4, 1)
                                .dm(UID2, 11, UID3, 9)
                                .dm(UID4, 11, UID3, 1))),
                ImmutableMap.of(
                        UID2, asList(Punkts, F2F, SetsBalance, F2F, BallsBalance,
                                F2F, UseDisambiguationMatches, Punkts),
                        UID3, asList(Punkts, F2F, SetsBalance, F2F, BallsBalance,
                                F2F, UseDisambiguationMatches, Punkts),
                        UID4, asList(Punkts, F2F, SetsBalance, F2F, BallsBalance,
                                F2F, UseDisambiguationMatches, Punkts)));
    }

    @Test
    public void orderUidsInGroupDisambiguates3ParticipantsAllDifferentStat() {
        checkOrder(asList(UID3, UID2, UID4),
                sut.findOrder(params(GID, tournamentForOrder(),
                        MatchListBuilder.matches()
                                .ogid(GID)
                                .om(UID2, 11, UID4, 1)
                                .om(UID2, 2, UID3, 11)
                                .om(UID4, 11, UID3, 3))));
    }

    @Test
    public void orderUidsInGroupRandomlyDisambiguates3Participants2OfThemHaveEqualStat() {
        checkOrder(asList(UID6, UID4, UID5, UID2, UID3),
                sut.findOrder(params(GID, tournamentForOrder(),
                        MatchListBuilder.matches().ogid(21)
                                .om(UID2, 11, UID4, 2)
                                .om(UID2, 11, UID5, 1)

                                .om(UID3, 11, UID2, 2)
                                .om(UID3, 11, UID4, 1)

                                .om(UID4, 11, UID6, 2)
                                .om(UID4, 11, UID5, 1)

                                .om(UID5, 11, UID3, 1)
                                .om(UID5, 11, UID6, 1)

                                .om(UID6, 11, UID2, 1)
                                .om(UID6, 11, UID3, 1))));
    }

    @Test
    public void orderUidsInGroupRandomlyDisambiguates3ParticipantsAllEqualStat() {
        final TournamentMemState tournament = tournamentForOrder();
        final GroupParticipantOrder order1 = randomOrder(tournament, GID);
        final GroupParticipantOrder order2 = randomOrder(tournament, GID2);
        assertNotEquals(order1.determinedUids(), order2.determinedUids());
        assertEquals(emptyList(), order1.ambiguousGroups());
        assertEquals(emptyList(), order2.ambiguousGroups());
    }

    private GroupParticipantOrder randomOrder(TournamentMemState tournament, int gid) {
        return sut.findOrder(params(gid, tournament,
                MatchListBuilder.matches().ogid(gid)
                        .om(UID2, 11, UID4, 0)
                        .om(UID2, 0, UID3, 11)
                        .om(UID4, 11, UID3, 0)));
    }
}
