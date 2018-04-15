package org.dan.ping.pong.app.match.rule;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.GID;
import static org.dan.ping.pong.app.group.GroupServiceTest.UID2;
import static org.dan.ping.pong.app.sport.SportType.PingPong;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.group.MatchListBuilder;
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
import java.util.Optional;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {GroupOrderRuleServiceCtx.class, SportCtx.class})
public class GroupParticipantOrderServiceTest {
    public static final Uid UID3 = new Uid(3);
    public static final Uid UID4 = new Uid(4);
    public static final Uid UID5 = new Uid(5);
    public static final int GID2 = 2;

    private static final Uid UID6 = new Uid(6);

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
        assertThat(got.getAmbiguousPositions(), is(emptySet()));
        assertThat(got.getPositions().values().stream()
                        .map(GroupPosition::getCompetingUids).collect(toList()),
                is(expectedOrder.stream().map(Collections::singleton)
                        .collect(toList())));
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
                                .m(UID2, 11, UID4, 1)
                                .m(UID2, 11, UID5, 1)

                                .m(UID2, 1, UID3, 11)
                                .m(UID4, 11, UID3, 1)

                                .m(UID5,  11, UID3, 1)
                                .m(UID5,  11, UID4, 1))));
    }

    @Test
    public void orderUidsInGroupDisambiguates3ParticipantsAllDifferentStat() {
        checkOrder(asList(UID3, UID2, UID4),
                sut.findOrder(params(GID, tournamentForOrder(),
                        MatchListBuilder.matches()
                                .ogid(GID)
                                .m(UID2, 11, UID4, 1)
                                .m(UID2, 2, UID3, 11)
                                .m(UID4, 11, UID3, 3))));
    }

    @Test
    public void orderUidsInGroupRandomlyDisambiguates3Participants2OfThemHaveEqualStat() {
        checkOrder(asList(UID6, UID2, UID4, UID5, UID3),
                sut.findOrder(params(GID, tournamentForOrder(),
                        MatchListBuilder.matches().ogid(21)
                                .m(UID2, 11, UID4, 2)
                                .m(UID2, 11, UID5, 1)

                                .m(UID3, 11, UID2, 2)
                                .m(UID3, 11, UID4, 1)

                                .m(UID4, 11, UID6, 2)
                                .m(UID4, 11, UID5, 1)

                                .m(UID5, 11, UID3, 1)
                                .m(UID5, 11, UID6, 1)

                                .m(UID6, 11, UID2, 1)
                                .m(UID6, 11, UID3, 1))));
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
                        .m(UID2, 11, UID4, 0)
                        .m(UID2, 0, UID3, 11)
                        .m(UID4, 11, UID3, 0)));
    }
}
