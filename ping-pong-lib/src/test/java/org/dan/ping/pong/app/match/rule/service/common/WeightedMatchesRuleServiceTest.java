package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.UID2;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID4;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WeightedMatches;
import static org.dan.ping.pong.app.match.rule.service.common.BallsBalanceRuleServiceTest.UIDS_2_3_4;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.group.HisIntPair;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.reason.WeightSetsReason;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.sport.SportCtx;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.util.collection.CmpValueCounter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.TreeSet;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WeightedMatchesRuleService.class, SportCtx.class})
public class WeightedMatchesRuleServiceTest {
    @Inject
    private WeightedMatchesRuleService sut;

    public static final List<MatchInfo> MATCHES_UIDS_2_3_4_NO_SAME = asList(
            MatchInfo.builder()
                    .participantIdScore(
                            ImmutableMap.of(UID2, asList(3, 2, 1),
                                    UID3, asList(6, 6, 6)))
                    .build(),
            MatchInfo.builder()
                    .participantIdScore(
                            ImmutableMap.of(UID3, asList(1, 6, 0, 1),
                                    UID4, asList(6, 1, 6, 6)))
                    .build(),
            MatchInfo.builder()
                    .participantIdScore(
                            ImmutableMap.of(UID4, asList(2, 6, 1, 6, 0),
                                    UID2, asList(6, 5, 6, 3, 6)))
                    .build());

    @Test
    public void test() {
        assertThat(
                sut.score(MATCHES_UIDS_2_3_4_NO_SAME::stream, UIDS_2_3_4, null,
                        GroupRuleParams.builder()
                                .tournament(TournamentMemState.builder()
                                        .sport(SportType.PingPong)
                                        .build())
                                .build())
                        .get().collect(toList()),
                is(asList(
                        new WeightSetsReason(UID3, WeightedMatches,
                                new TreeSet<>(asList(
                                        new CmpValueCounter<>(new HisIntPair(3, 0), 1),
                                        new CmpValueCounter<>(new HisIntPair(1, 3), 1)))),
                        new WeightSetsReason(UID4, WeightedMatches,
                                new TreeSet<>(asList(
                                        new CmpValueCounter<>(new HisIntPair(3, 1), 1),
                                        new CmpValueCounter<>(new HisIntPair(2, 3), 1)))),
                        new WeightSetsReason(UID2, WeightedMatches,
                                new TreeSet<>(asList(
                                        new CmpValueCounter<>(new HisIntPair(3, 2), 1),
                                        new CmpValueCounter<>(new HisIntPair(0, 3), 1)))))));
    }
}
