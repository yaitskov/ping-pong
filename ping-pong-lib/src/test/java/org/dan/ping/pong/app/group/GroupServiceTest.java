package org.dan.ping.pong.app.group;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.dan.ping.pong.app.group.DisambiguationPolicy.CMP_WIN_MINUS_LOSE;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
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

import java.util.Collections;
import java.util.Optional;

public class GroupServiceTest {
    private static final int CID = 1;
    private static final int GID = 1;
    private static final Uid UID1 = new Uid(2);
    private static final Uid UID2 = new Uid(3);
    private static final Uid UID3 = new Uid(4);

    GroupService sut;

    @Before
    public void setUp() {
        sut = new GroupService();
    }

    @Test
    public void zeroPopulationsNoGroups() {
        assertThat(sut.populations(TournamentMemState.builder()
                        .participants(ImmutableMap.of(UID1,
                                ParticipantMemState.builder()
                                        .gid(Optional.empty())
                                        .uid(UID1)
                                        .cid(CID)
                                        .tid(new Tid(1))
                                        .build()))
                        .groups(Collections.emptyMap())
                        .build(), CID),
                allOf(hasProperty("links", hasSize(0)),
                        hasProperty("populations", hasSize(0))));
    }

    @Test
    public void populations1Person() {
        assertThat(sut.populations(TournamentMemState.builder()
                        .participants(ImmutableMap.of(UID1, ParticipantMemState.builder()
                                .gid(Optional.of(GID))
                                .uid(UID1)
                                .cid(CID)
                                .tid(new Tid(1))
                                .build()))
                        .groups(ImmutableMap.of(GID, GroupInfo.builder().cid(CID).gid(GID).build()))
                        .build(), CID),
                allOf(hasProperty("links", hasSize(1)),
                        hasProperty("populations", is(singletonList(1L)))));
    }

    public static final MatchValidationRule S1A2G11 = MatchValidationRule.builder()
            .setsToWin(1)
            .minAdvanceInGames(2)
            .minPossibleGames(0)
            .minGamesToWin(11)
            .build();

    @Test
    public void findStrongerExtraOrder3ParticipantsEveryHas1Win() {
        final TournamentMemState tournament = TournamentMemState.builder()
                .rule(TournamentRules.builder()
                        .match(S1A2G11)
                        .group(
                                Optional.of(GroupRules.builder()
                                        .disambiguation(CMP_WIN_MINUS_LOSE)
                                        .build()))
                        .build())
                .build();

        final SetMultimap<Uid, Uid> got = sut.findStrongerExtraOrder(tournament,
                ImmutableMap.<Uid, Integer>builder().put(UID1, 1).put(UID2, 1).put(UID3, 1).build(),
                asList(MatchInfo.builder()
                                .participantIdScore(ImmutableMap.of(
                                        UID1, singletonList(11),
                                        UID2, singletonList(0)))
                                .winnerId(Optional.of(UID1))
                                .build(),
                        MatchInfo.builder()
                                .participantIdScore(ImmutableMap.of(
                                        UID3, singletonList(1),
                                        UID2, singletonList(11)))
                                .winnerId(Optional.of(UID2))
                                .build(),
                        MatchInfo.builder()
                                .participantIdScore(ImmutableMap.of(
                                        UID3, singletonList(14),
                                        UID1, singletonList(12)))
                                .winnerId(Optional.of(UID3))
                                .build()));

        assertEquals(got.get(UID1), ImmutableSet.of(UID2, UID3));
        assertEquals(got.get(UID2), ImmutableSet.of(UID3));
    }
}
