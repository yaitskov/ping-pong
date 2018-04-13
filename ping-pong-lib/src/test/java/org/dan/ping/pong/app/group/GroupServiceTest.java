package org.dan.ping.pong.app.group;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.dan.ping.pong.app.sport.SportType.PingPong;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;
import org.dan.ping.pong.app.sport.pingpong.PingPongSport;
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
    public static final int GID2 = 2;

    GroupService sut;

    @Before
    public void setUp() {
        sut = GroupService
                .builder()
                .sports(new Sports(ImmutableMap.of(PingPong, new PingPongSport())))
                .build();
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

    public static final PingPongMatchRules S1A2G11 = PingPongMatchRules.builder()
            .setsToWin(1)
            .minAdvanceInGames(2)
            .minPossibleGames(0)
            .minGamesToWin(11)
            .build();

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
    public void orderUidsInGroupDisambiguates2Participants() {
        final TournamentMemState tournament = tournamentForOrder();

        assertEquals(asList(UID1, UID4, UID3, UID2),
                sut.orderUidsInGroup(GID, tournament,
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
                .sport(PingPong)
                .rule(TournamentRules.builder()
                        .match(S1A2G11)
                        .group(Optional.of(GroupRules.builder().build()))
                        .build())
                .build();
    }

    @Test
    public void orderUidsInGroupDisambiguates3ParticipantsAllDifferentStat() {
        final TournamentMemState tournament = tournamentForOrder();

        assertEquals(asList(UID2, UID1, UID3),
                sut.orderUidsInGroup(GID, tournament,
                        MatchListBuilder.matches()
                                .ogid(GID)
                                .m(UID1, 11, UID3, 1)
                                .m(UID1, 2, UID2, 11)
                                .m(UID3, 11, UID2, 3)
                                .build()));
    }

    @Test
    public void orderUidsInGroupRandomlyDisambiguates3ParticipantsAllEqualStat() {
        final TournamentMemState tournament = tournamentForOrder();

        final List<Uid> order1 = sut.orderUidsInGroup(GID, tournament,
                MatchListBuilder.matches().ogid(GID)
                        .m(UID1, 11, UID3, 0)
                        .m(UID1, 0, UID2, 11)
                        .m(UID3, 11, UID2, 0)
                        .build());

        final List<Uid> order2 = sut.orderUidsInGroup(GID2, tournament,
                MatchListBuilder.matches().ogid(GID2)
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
                sut.orderUidsInGroup(GID, tournament,
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
