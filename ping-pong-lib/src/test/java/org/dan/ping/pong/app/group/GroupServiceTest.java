package org.dan.ping.pong.app.group;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.dan.ping.pong.app.group.DisambiguationPolicy.CMP_WIN_MINUS_LOSE;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchValidationRule;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GroupServiceTest {
    private static final int CID = 1;
    private static final int GID = 1;
    private static final Uid UID1 = new Uid(2);
    private static final Uid UID2 = new Uid(3);
    private static final Uid UID3 = new Uid(4);
    private static final Uid UID4 = new Uid(5);
    private static final Uid UID5 = new Uid(6);

    GroupService sut;

    @Before
    public void setUp() {
        sut = new GroupService();
    }

    @Test
    public void zeroPopulationsNoGroups() {
        assertThat(sut.populations(tournamentForPopulations(Optional.empty(), Collections.emptyMap()), CID),
                allOf(hasProperty("links", hasSize(0)),
                        hasProperty("populations", hasSize(0))));
    }

    @Test
    public void populations1Person() {
        assertThat(sut.populations(tournamentForPopulations(Optional.of(GID),
                ImmutableMap.of(GID, GroupInfo.builder().cid(CID).gid(GID).build())), CID),
                allOf(hasProperty("links", hasSize(1)),
                        hasProperty("populations", is(singletonList(1L)))));
    }

    private TournamentMemState tournamentForPopulations(
            Optional<Integer> gid, Map<Integer, GroupInfo> groups) {
        return TournamentMemState.builder()
                        .participants(ImmutableMap.of(UID1, ParticipantMemState.builder()
                                .gid(gid)
                                .uid(UID1)
                                .cid(CID)
                                .tid(new Tid(1))
                                .build()))
                        .groups(groups).build();
    }

    public static final MatchValidationRule S1A2G11 = MatchValidationRule.builder()
            .setsToWin(1)
            .minAdvanceInGames(2)
            .minPossibleGames(0)
            .minGamesToWin(11)
            .build();

    @Test
    public void findStrongerExtraOrder3ParticipantsEveryHas1Win() {
        final TournamentMemState tournament = tournamentForOrder();

        final SetMultimap<Uid, Uid> got = sut.findStrongerExtraOrder(tournament,
                ImmutableMap.<Uid, Integer>builder().put(UID1, 1).put(UID2, 1).put(UID3, 1).build(),
                MatchListBuilder.matches()
                        .m(UID1, 11, UID2, 0)
                        .m(UID3, 1, UID2, 11)
                        .m(UID3, 14, UID1, 12)
                        .build());

        assertEquals(ImmutableSet.of(UID2, UID3), got.get(UID1));
        assertEquals(ImmutableSet.of(UID3), got.get(UID2));
    }

    private static class MatchListBuilder {
        private Optional<Integer> ogid;

        final List<MatchInfo> result = new ArrayList<>();
        public static MatchListBuilder matches() {
            return new MatchListBuilder();
        }

        public MatchListBuilder ogid(Integer gid) {
            ogid = Optional.of(gid);
            return this;
        }

        public MatchListBuilder m(Uid u1, Integer g1, Uid u2, Integer g2) {
            result.add(match(u1, g1, u2, g2));
            return this;
        }

        private MatchInfo match(Uid uid1, int g1, Uid uid2, int g2) {
            return MatchInfo.builder()
                    .gid(ogid)
                    .participantIdScore(ImmutableMap.of(
                            uid1, singletonList(g1),
                            uid2, singletonList(g2)))
                    .winnerId(Optional.of(g1 > g2 ? uid1 : uid2))
                    .build();
        }

        public List<MatchInfo> build() {
            return result;
        }
    }

    @Test
    public void countPointsUidWithoutWins0() {
        Map<Uid, Integer> got = sut.countPoints(asList(MatchInfo.builder()
                .winnerId(Optional.of(UID1))
                .participantIdScore(ImmutableMap.of(UID1, singletonList(11),
                        UID2, singletonList(0)))
                .build()));
        assertEquals(ImmutableMap.of(UID1, 1, UID2, 0), got);
    }

    @Test
    public void orderUidsInGroupDisambiguates2Participants() {
        final TournamentMemState tournament = tournamentForOrder();

        assertEquals(asList(UID1, UID4, UID3, UID2),
                sut.orderUidsInGroup(tournament,
                        MatchListBuilder.matches()
                                .m(UID1, 11, UID3, 1)
                                .m(UID1, 11, UID4, 1)

                                .m(UID1, 1, UID2, 11)
                                .m(UID3, 11, UID2, 1)

                                .m(UID4,  11, UID2, 1)
                                .m(UID4,  11, UID3, 1)

                                .build()));
    }

    private TournamentMemState tournamentForOrder() {
        return TournamentMemState.builder()
                .rule(TournamentRules.builder()
                        .match(S1A2G11)
                        .group(
                                Optional.of(GroupRules.builder()
                                        .disambiguation(CMP_WIN_MINUS_LOSE)
                                        .build()))
                        .build())
                .build();
    }

    @Test
    public void orderUidsInGroupDisambiguates3ParticipantsAllDifferentStat() {
        final TournamentMemState tournament = tournamentForOrder();

        assertEquals(asList(UID2, UID1, UID3),
                sut.orderUidsInGroup(tournament,
                        MatchListBuilder.matches()
                                .m(UID1, 11, UID3, 1)
                                .m(UID1, 2, UID2, 11)
                                .m(UID3, 11, UID2, 3)
                                .build()));
    }

    @Test
    public void orderUidsInGroupRandomlyDisambiguates3ParticipantsAllEqualStat() {
        final TournamentMemState tournament = tournamentForOrder();

        final List<Uid> order1 = sut.orderUidsInGroup(tournament,
                MatchListBuilder.matches().ogid(1)
                        .m(UID1, 11, UID3, 0)
                        .m(UID1, 0, UID2, 11)
                        .m(UID3, 11, UID2, 0)
                        .build());

        final List<Uid> order2 = sut.orderUidsInGroup(tournament,
                MatchListBuilder.matches().ogid(2)
                        .m(UID1, 11, UID3, 0)
                        .m(UID1, 0, UID2, 11)
                        .m(UID3, 11, UID2, 0)
                        .build());

        assertNotEquals(order1, order2);
    }

    @Test
    public void orderUidsInGroupRandomlyDisambiguates3Participants2OfThemHaveEqualStat() {
        final TournamentMemState tournament = tournamentForOrder();

        assertEquals(asList(UID5, UID1, UID3, UID4, UID2),
                sut.orderUidsInGroup(tournament,
                        MatchListBuilder.matches().ogid(21)
                                .m(UID1, 11, UID3, 2)
                                .m(UID1, 11, UID4, 1)

                                .m(UID2, 11, UID1, 2)
                                .m(UID2, 11, UID3, 1)

                                .m(UID3, 11, UID5, 2)
                                .m(UID3, 11, UID4, 1)

                                .m(UID4, 11, UID2, 1)
                                .m(UID4, 11, UID5, 1)

                                .m(UID5, 11, UID1, 1)
                                .m(UID5, 11, UID2, 1)

                                .build()));
    }
}
