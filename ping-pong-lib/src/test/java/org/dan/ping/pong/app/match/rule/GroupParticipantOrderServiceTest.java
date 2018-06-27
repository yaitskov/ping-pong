package org.dan.ping.pong.app.match.rule;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.BID2;
import static org.dan.ping.pong.app.group.GroupServiceTest.GID;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.BallsBalance;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.F2F;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.Punkts;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.SetsBalance;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.UseDisambiguationMatches;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonMatches;
import static org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope.JUST_NORMALLY_COMPLETE;
import static org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope.BOTH;
import static org.dan.ping.pong.app.match.rule.rules.common.DirectOutcomeRule.DIRECT_OUTCOME_RULE;
import static org.dan.ping.pong.app.match.rule.service.common.BallsBalanceRuleServiceTest.UIDS_2_3_4;
import static org.dan.ping.pong.app.sport.SportType.PingPong;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.group.MatchListBuilder;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.rules.common.BallsBalanceRule;
import org.dan.ping.pong.app.match.rule.rules.common.CountWonMatchesRule;
import org.dan.ping.pong.app.match.rule.rules.common.DirectOutcomeRule;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    public static final Bid BID3 = new Bid(3);
    public static final Uid UID3 = new Uid(3);
    public static final Bid BID4 = new Bid(4);
    public static final Uid UID4 = new Uid(4);
    public static final Bid BID5 = new Bid(5);
    public static final int GID2 = 2;
    public static final Bid BID6 = new Bid(6);

    public static final Set<Bid> UIDS_2_3_4_5 = ImmutableSet.of(BID2, BID3, BID4, BID5);
    private static final Set<Bid> UIDS_2_3_4_5_6 = ImmutableSet.of(BID2, BID3, BID4, BID5, BID6);

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

    private void checkOrder(List<Set<Bid>> expectedOrder, GroupParticipantOrder got) {
        checkOrder(expectedOrder, got, emptyMap(), emptySet());
    }

    private void checkOrder(List<Set<Bid>> expectedOrder, GroupParticipantOrder got,
            Set<Object> ambitionsPositions) {
        checkOrder(expectedOrder, got, emptyMap(), ambitionsPositions);
    }

    private void checkOrder(List<Set<Bid>> expectedOrder, GroupParticipantOrder got,
            Map<Bid, List<OrderRuleName>> reasonChains, Set<Object> ambitionsPositions) {
        assertThat(got.getAmbiguousPositions(), is(ambitionsPositions));
        assertThat(got.getPositions().values().stream()
                        .map(GroupPosition::getCompetingBids).collect(toList()),
                is(expectedOrder));
        got.getPositions().values()
                .forEach(gp -> {
                    final List<OrderRuleName> reasonChain = gp.reasonChain().stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(Reason::getRule)
                            .collect(toList());
                    gp.getCompetingBids().forEach(uid -> {
                        final List<OrderRuleName> expected = reasonChains.get(uid);
                        if (expected == null) {
                            return;
                        }
                        assertThat("uid " + uid, reasonChain, is(expected));
                    });
                });
    }

    private GroupRuleParams params(int gid, TournamentMemState tournament,
            MatchListBuilder matchListBuilder, Set<Bid> bids) {
        return GroupRuleParams.ofParams(gid, tournament,
                matchListBuilder.build(), tournament.orderRules(), new HashSet<>(bids));
    }

    @Test
    public void orderUidsInGroupDisambiguates2Participants() {
        checkOrder(uidSets(BID2, BID5, BID4, BID3),
                sut.findOrder(params(GID, tournamentForOrder(),
                        MatchListBuilder.matches()
                                .om(BID2, 11, BID4, 1)
                                .om(BID2, 11, BID5, 1)

                                .om(BID2, 1, BID3, 11)
                                .om(BID4, 11, BID3, 1)

                                .om(BID5,  11, BID3, 1)
                                .om(BID5,  11, BID4, 1), UIDS_2_3_4_5)));
    }

    @Test
    public void orderUidsWithDisambiguateMatches() {
        final TournamentMemState tournament = tournamentForOrder();
        tournament.getRule().getGroup().get()
                .setOrderRules(BALANCE_BASED_DM_ORDER_RULES);
        checkOrder(uidSets(BID2, BID4, BID3),
                sut.findOrder(params(GID, tournament,
                        MatchListBuilder.matches()
                                .om(BID2, 11, BID4, 1)
                                .om(BID2, 1, BID3, 11)
                                .om(BID4, 11, BID3, 1)

                                .dm(BID2, 11, BID4, 1)
                                .dm(BID2, 11, BID3, 9)
                                .dm(BID4, 11, BID3, 1), UIDS_2_3_4)),
                ImmutableMap.of(
                        BID2, asList(Punkts, F2F, SetsBalance, F2F, BallsBalance,
                                F2F, UseDisambiguationMatches, Punkts),
                        BID3, asList(Punkts, F2F, SetsBalance, F2F, BallsBalance,
                                F2F, UseDisambiguationMatches, Punkts),
                        BID4, asList(Punkts, F2F, SetsBalance, F2F, BallsBalance,
                                F2F, UseDisambiguationMatches, Punkts)), emptySet());
    }

    private List<Set<Bid>> uidSets(Bid... bids) {
        return Arrays.stream(bids).map(Collections::singleton).collect(toList());
    }

    @Test
    public void orderUidsWithNoDisambiguateMatches() {
        final TournamentMemState tournament = tournamentForOrder();
        tournament.getRule().getGroup().get()
                .setOrderRules(BALANCE_BASED_DM_ORDER_RULES);
        checkOrder(singletonList(ImmutableSet.of(BID2, BID4, BID3)),
                sut.findOrder(params(GID, tournament,
                        MatchListBuilder.matches()
                                .om(BID2, 11, BID4, 1)
                                .om(BID2, 1, BID3, 11)
                                .om(BID4, 11, BID3, 1), UIDS_2_3_4)),
                singleton(new GroupPositionIdx(0)));
    }

    @Test
    public void filterBrokenMatch() {
        final TournamentMemState tournament = tournamentForOrder();
        tournament.getRule().getGroup().get().getOrderRules().stream()
                .filter(rule -> rule.name() == WonMatches)
                .findFirst()
                .get()
                .setMatchOutcomeScope(JUST_NORMALLY_COMPLETE);

        checkOrder(uidSets(BID2, BID4, BID3),
                sut.findOrder(params(GID, tournament,
                        MatchListBuilder.matches()
                                .ogid(GID)
                                .om(BID2, 11, BID4, 1)
                                .brokenMatch(BID2, 2, BID3, 10)
                                .om(BID4, 11, BID3, 1), UIDS_2_3_4)),
                ImmutableMap.of(
                        BID2, asList(WonMatches, F2F),
                        BID4, asList(WonMatches, F2F),
                        BID3, singletonList(WonMatches)),
                emptySet());
    }

    @Test
    public void keepUidsTopRuleFilteredMatches() {
        final TournamentMemState tournament = tournamentForOrder();
        tournament.getRule().getGroup().get().getOrderRules().stream()
                .filter(rule -> rule.name() == WonMatches)
                .findFirst()
                .get()
                .setMatchOutcomeScope(JUST_NORMALLY_COMPLETE);

        checkOrder(uidSets(BID4, BID3, BID2),
                sut.findOrder(params(GID, tournament,
                        MatchListBuilder.matches()
                                .ogid(GID)
                                .brokenMatch(BID2, 10, BID4, 1)
                                .brokenMatch(BID2, 2, BID3, 10)
                                .om(BID4, 11, BID3, 1), UIDS_2_3_4)),
                ImmutableMap.of(
                        BID4, singletonList(WonMatches),
                        BID3, singletonList(WonMatches),
                        BID2, singletonList(WonMatches)),
                emptySet());
    }

    @Test
    public void allMatchesToNormallyComplete() {
        final TournamentMemState tournament = tournamentForOrder();
        List<GroupOrderRule> rules = asList(new CountWonMatchesRule(),
                new CountWonMatchesRule(), new DirectOutcomeRule());
        rules.get(1).setMatchOutcomeScope(JUST_NORMALLY_COMPLETE);
        tournament.getRule().getGroup().get().setOrderRules(rules);

        checkOrder(uidSets(BID4, BID3, BID2),
                sut.findOrder(params(GID, tournament,
                        MatchListBuilder.matches()
                                .ogid(GID)
                                .brokenMatch(BID2, 10, BID4, 1)
                                .om(BID2, 1, BID3, 11)
                                .om(BID4, 11, BID3, 1), UIDS_2_3_4)),
                ImmutableMap.of(
                        BID4, asList(WonMatches, WonMatches, F2F),
                        BID3, asList(WonMatches, WonMatches, F2F),
                        BID2, asList(WonMatches, WonMatches)),
                emptySet());
    }

    @Test
    public void normallyCompleteToAllMatches() {
        final TournamentMemState tournament = tournamentForOrder();
        List<GroupOrderRule> rules = asList(new CountWonMatchesRule(), new CountWonMatchesRule());
        rules.get(0).setMatchOutcomeScope(JUST_NORMALLY_COMPLETE);
        tournament.getRule().getGroup().get().setOrderRules(rules);

        checkOrder(uidSets(BID4, BID3, BID2),
                sut.findOrder(params(GID, tournament,
                        MatchListBuilder.matches()
                                .ogid(GID)
                                .brokenMatch(BID2, 0, BID4, 10)
                                .om(BID2, 1, BID3, 11)
                                .om(BID4, 11, BID3, 1), UIDS_2_3_4)),
                ImmutableMap.of(
                        BID4, asList(WonMatches, WonMatches),
                        BID3, asList(WonMatches, WonMatches),
                        BID2, singletonList(WonMatches)),
                emptySet());
    }

    @Test
    public void bothToAtLeastOne() {
        final TournamentMemState tournament = tournamentForOrder();
        final List<GroupOrderRule> rules = asList(new CountWonMatchesRule(),
                new CountWonMatchesRule(), new BallsBalanceRule());
        rules.get(1).setMatchParticipantScope(BOTH);
        tournament.getRule().getGroup().get().setOrderRules(rules);

        checkOrder(uidSets(BID2, BID5, BID4, BID3),
                sut.findOrder(params(GID, tournament,
                        MatchListBuilder.matches()
                                .ogid(GID)
                                .om(BID4, 11, BID2, 9) // 2
                                .om(BID4, 11, BID3, 1)

                                .om(BID2, 11, BID3, 3)
                                .om(BID2, 11, BID5, 9) // 2

                                .om(BID5, 11, BID3, 9)
                                .om(BID5, 11, BID4, 4), // 2
                        // uid5 balls balance 7 ; 5
                        // uid2 balls balance 8  ; 0
                        // uid4 balls balance 5  ; -
                        UIDS_2_3_4_5)),
                ImmutableMap.of(
                        BID5, asList(WonMatches, WonMatches, BallsBalance),
                        BID4, asList(WonMatches, WonMatches, BallsBalance),
                        BID2, asList(WonMatches, WonMatches, BallsBalance),
                        BID3, singletonList(WonMatches)),
                emptySet());
    }

    @Test
    public void orderUidsInGroupDisambiguates3ParticipantsAllDifferentStat() {
        checkOrder(uidSets(BID3, BID2, BID4),
                sut.findOrder(params(GID, tournamentForOrder(),
                        MatchListBuilder.matches()
                                .ogid(GID)
                                .om(BID2, 11, BID4, 1)
                                .om(BID2, 2, BID3, 11)
                                .om(BID4, 11, BID3, 3), UIDS_2_3_4)));
    }

    @Test
    public void orderUidsInGroupRandomlyDisambiguates3Participants2OfThemHaveEqualStat() {
        checkOrder(uidSets(BID6, BID4, BID5, BID2, BID3),
                sut.findOrder(params(GID, tournamentForOrder(),
                        MatchListBuilder.matches().ogid(21)
                                .om(BID2, 11, BID4, 2)
                                .om(BID2, 11, BID5, 1)

                                .om(BID3, 11, BID2, 2)
                                .om(BID3, 11, BID4, 1)

                                .om(BID4, 11, BID6, 2)
                                .om(BID4, 11, BID5, 1)

                                .om(BID5, 11, BID3, 1)
                                .om(BID5, 11, BID6, 1)

                                .om(BID6, 11, BID2, 1)
                                .om(BID6, 11, BID3, 1), UIDS_2_3_4_5_6)));
    }

    @Test
    public void orderUidsInGroupRandomlyDisambiguates3ParticipantsAllEqualStat() {
        final TournamentMemState tournament = tournamentForOrder();
        final GroupParticipantOrder order1 = randomOrder(tournament, GID);
        final GroupParticipantOrder order2 = randomOrder(tournament, GID2);
        assertNotEquals(order1.determinedBids(), order2.determinedBids());
        assertEquals(emptyList(), order1.ambiguousGroups());
        assertEquals(emptyList(), order2.ambiguousGroups());
    }

    private GroupParticipantOrder randomOrder(TournamentMemState tournament, int gid) {
        return sut.findOrder(params(gid, tournament,
                MatchListBuilder.matches().ogid(gid)
                        .om(BID2, 11, BID4, 0)
                        .om(BID2, 0, BID3, 11)
                        .om(BID4, 11, BID3, 0), UIDS_2_3_4));
    }
}
