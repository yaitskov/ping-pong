package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.BID2;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID4;
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

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WeightedMatchesRuleService.class, SportCtx.class})
public class WeightedMatchesRuleServiceTest {
    @Inject
    private WeightedMatchesRuleService sut;

    public static final List<MatchInfo> MATCHES_UIDS_2_3_4_NO_SAME = asList(
            MatchInfo.builder()
                    .participantIdScore(
                            ImmutableMap.of(BID2, asList(3, 2, 1),
                                    BID3, asList(6, 6, 6)))
                    .build(),
            MatchInfo.builder()
                    .participantIdScore(
                            ImmutableMap.of(BID3, asList(1, 6, 0, 1),
                                    BID4, asList(6, 1, 6, 6)))
                    .build(),
            MatchInfo.builder()
                    .participantIdScore(
                            ImmutableMap.of(BID4, asList(2, 6, 1, 6, 0),
                                    BID2, asList(6, 5, 6, 3, 6)))
                    .build());

    @Test
    public void test() {
        final List<WeightSetsReason> result = sut.score(
                MATCHES_UIDS_2_3_4_NO_SAME::stream, UIDS_2_3_4, null,
                GroupRuleParams.builder()
                        .tournament(TournamentMemState.builder()
                                .sport(SportType.PingPong)
                                .build())
                        .build())
                .get()
                .map(WeightSetsReason.class::cast)
                .collect(toList());
        assertThat(result.stream().map(WeightSetsReason::getBid).collect(toList()),
                is(asList(BID3, BID4, BID2)));
        assertThat(result.stream().map(WeightSetsReason::getRule).collect(toList()),
                is(asList(WeightedMatches, WeightedMatches, WeightedMatches)));

        assertThat(result.get(0).getWeightSets().stream().collect(toList()),
                is(asList(
                        new CmpValueCounter<>(new HisIntPair(3, 0), 1),
                        new CmpValueCounter<>(new HisIntPair(1, 3), 1))));

        assertThat(result.get(1).getWeightSets().stream().collect(toList()),
                is(asList(
                        new CmpValueCounter<>(new HisIntPair(3, 1), 1),
                        new CmpValueCounter<>(new HisIntPair(2, 3), 1))));

        assertThat(result.get(2).getWeightSets().stream().collect(toList()),
                is(asList(
                        new CmpValueCounter<>(new HisIntPair(3, 2), 1),
                        new CmpValueCounter<>(new HisIntPair(0, 3), 1))));
    }
}
