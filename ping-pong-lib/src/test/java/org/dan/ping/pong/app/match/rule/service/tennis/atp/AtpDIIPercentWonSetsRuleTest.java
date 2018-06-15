package org.dan.ping.pong.app.match.rule.service.tennis.atp;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.UID2;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID4;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID5;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpDII;
import static org.dan.ping.pong.app.match.rule.service.tennis.atp.AtpDIRuleServiceTest.UIDS_3_4_5;
import static org.dan.ping.pong.app.sport.tennis.TennisSportTest.CLASSIC_TENNIS_RULES;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.reason.DecreasingDoubleScalarReason;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.match.rule.service.common.LostSetsRuleService;
import org.dan.ping.pong.app.match.rule.service.common.WonSetsRuleService;
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

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        AtpDIIPercentWonSetsRuleService.class,
        WonSetsRuleService.class, LostSetsRuleService.class,
        SportCtx.class})
public class AtpDIIPercentWonSetsRuleTest {
    @Inject
    private AtpDIIPercentWonSetsRuleService sut;

    static final List<MatchInfo> BASE = asList(
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(UID3))
                    .gid(Optional.of(1))
                    .tag(Optional.empty())
                    .participantIdScore(ImmutableMap.of(
                            UID3, asList(6, 6),
                            UID2, asList(0, 0)))
                    .build(),
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(UID4))
                    .gid(Optional.of(1))
                    .tag(Optional.empty())
                    .participantIdScore(
                            ImmutableMap.of(UID4, asList(6, 1, 6),
                                    UID2, asList(0, 6, 0)))
                    .build(),
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(UID5))
                    .gid(Optional.of(1))
                    .tag(Optional.empty())
                    .participantIdScore(
                            ImmutableMap.of(UID5, asList(6, 6),
                                    UID2, asList(0, 0)))
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

    @Test
    public void test() {
        final List<DecreasingDoubleScalarReason> result = sut.score(BASE::stream, UIDS_3_4_5, null,
                GroupRuleParams.builder()
                        .tournament(TournamentMemState.builder()
                                .sport(SportType.Tennis)
                                .rule(TournamentRules.builder()
                                        .match(CLASSIC_TENNIS_RULES)
                                        .build())
                                .build())
                        .build())
                .get()
                .map(DecreasingDoubleScalarReason.class::cast)
                .collect(toList());
        assertThat(
                result.stream()
                        .map(DecreasingDoubleScalarReason::getValue)
                        .collect(toList()),
                contains(closeTo(4.0 / 6.0, 1e-5),
                        closeTo(4.0 / 6.0, 1e-5),
                        closeTo(4.0 / 7.0, 1e-5),
                        closeTo(1.0 / 7.0, 1e-5)));

        assertThat(
                result.stream()
                        .map(DecreasingDoubleScalarReason::getRule)
                        .collect(toList()),
                everyItem(is(AtpDII)));
        assertThat(
                result.stream()
                        .map(DecreasingDoubleScalarReason::getUid)
                        .collect(toList()),
                contains(UID5, UID3, UID4, UID2));
    }
}
