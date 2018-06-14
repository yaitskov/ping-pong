package org.dan.ping.pong.app.match.rule.service.tennis.atp;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.UID2;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID4;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID5;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpDI;
import static org.dan.ping.pong.app.match.rule.reason.IncreasingIntScalarReason.ofIntI;
import static org.dan.ping.pong.app.sport.tennis.TennisSportTest.CLASSIC_TENNIS_RULES;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.sport.SportCtx;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AtpDIRuleService.class, SportCtx.class})
public class AtpDIRuleServiceTest {
    @Inject
    private AtpDIRuleService sut;

    public static final Set<Uid> UIDS_3_4_5 = ImmutableSet.of(UID3, UID4, UID5);

    private static final List<MatchInfo> BASE = asList(
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(UID2))
                    .gid(Optional.of(1))
                    .tag(Optional.empty())
                    .participantIdScore(
                            ImmutableMap.of(UID2, asList(6, 6),
                                    UID4, asList(0, 0)))
                    .build(),
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(UID2))
                    .gid(Optional.of(1))
                    .tag(Optional.empty())
                    .participantIdScore(
                            ImmutableMap.of(UID2, asList(6, 6),
                                    UID5, asList(0, 0)))
                    .build(),
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(UID3))
                    .gid(Optional.of(1))
                    .tag(Optional.empty())
                    .participantIdScore(
                            ImmutableMap.of(UID3, asList(6, 6),
                                    UID4, asList(0, 0)))
                    .build(),
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(UID5))
                    .gid(Optional.of(1))
                    .tag(Optional.empty())
                    .participantIdScore(
                            ImmutableMap.of(UID3, asList(3, 2),
                                    UID5, asList(6, 6)))
                    .build(),
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(UID4))
                    .gid(Optional.of(1))
                    .tag(Optional.empty())
                    .participantIdScore(
                            ImmutableMap.of(UID4, asList(6, 6),
                                    UID5, asList(0, 0)))
                    .build());

    public static final List<MatchInfo> _3_BY_1_WIN_WITH_WALKOVER =
            ImmutableList.<MatchInfo>builder()
                    .add(
                            MatchInfo.builder().state(Over)
                                    .winnerId(Optional.of(UID2))
                                    .gid(Optional.of(1))
                                    .tag(Optional.empty())
                                    .participantIdScore(ImmutableMap.of(
                                            UID2, emptyList(),
                                            UID3, emptyList()))
                                    .build()).
                    addAll(BASE)
                    .build();

    public static final List<MatchInfo> _3_BY_1_WIN_NO_WALKOVER =
            ImmutableList.<MatchInfo>builder()
                    .add(
                            MatchInfo.builder().state(Over)
                                    .winnerId(Optional.of(UID2))
                                    .gid(Optional.of(1))
                                    .tag(Optional.empty())
                                    .participantIdScore(ImmutableMap.of(
                                            UID2, asList(6, 6),
                                            UID3, asList(0, 0)))
                                    .build()).
                    addAll(BASE)
                    .build();

    @Test
    public void test3UsersBy1WinAnd1WalkOver() {
        assertThat(
                sut.score(_3_BY_1_WIN_WITH_WALKOVER::stream, UIDS_3_4_5, null,
                        GroupRuleParams.builder()
                                .tournament(TournamentMemState.builder()
                                        .sport(SportType.Tennis)
                                        .rule(TournamentRules.builder()
                                                .match(CLASSIC_TENNIS_RULES)
                                                .build())
                                        .build())
                                .build())
                        .get().collect(toList()),
                is(asList(ofIntI(UID4, 1, AtpDI),
                        ofIntI(UID5, 1, AtpDI),
                        ofIntI(UID3, 2, AtpDI))));
    }

    @Test
    public void test3UsersBy1WinAndNoWalkOver() {
        assertThat(
                sut.score(_3_BY_1_WIN_NO_WALKOVER::stream, UIDS_3_4_5, null,
                        GroupRuleParams.builder()
                                .tournament(TournamentMemState.builder()
                                        .sport(SportType.Tennis)
                                        .rule(TournamentRules.builder()
                                                .match(CLASSIC_TENNIS_RULES)
                                                .build())
                                        .build())
                                .build()),
                is(Optional.empty()));
    }
}
