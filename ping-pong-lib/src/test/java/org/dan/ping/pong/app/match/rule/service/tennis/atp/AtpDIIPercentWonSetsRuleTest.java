package org.dan.ping.pong.app.match.rule.service.tennis.atp;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.BID2;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID4;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID5;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpDII;
import static org.dan.ping.pong.app.match.rule.service.tennis.atp.AtpDIRuleServiceTest.BIDS_3_4_5;
import static org.dan.ping.pong.app.sport.tennis.TennisSportTest.CLASSIC_TENNIS_RULES;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.group.Gid;
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

    static final Optional<Gid> OGID = Optional.of(Gid.of(1));
    static final List<MatchInfo> BASE = asList(
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(BID3))
                    .gid(OGID)
                    .tag(Optional.empty())
                    .participantIdScore(ImmutableMap.of(
                            BID3, asList(6, 6),
                            BID2, asList(0, 0)))
                    .build(),
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(BID4))
                    .gid(OGID)
                    .tag(Optional.empty())
                    .participantIdScore(
                            ImmutableMap.of(BID4, asList(6, 1, 6),
                                    BID2, asList(0, 6, 0)))
                    .build(),
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(BID5))
                    .gid(OGID)
                    .tag(Optional.empty())
                    .participantIdScore(
                            ImmutableMap.of(BID5, asList(6, 6),
                                    BID2, asList(0, 0)))
                    .build(),

            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(BID3))
                    .gid(OGID)
                    .tag(Optional.empty())
                    .participantIdScore(
                            ImmutableMap.of(BID3, asList(6, 6),
                                    BID4, asList(0, 0)))
                    .build(),
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(BID5))
                    .gid(OGID)
                    .tag(Optional.empty())
                    .participantIdScore(
                            ImmutableMap.of(BID3, asList(3, 2),
                                    BID5, asList(6, 6)))
                    .build(),
            MatchInfo.builder().state(Over)
                    .winnerId(Optional.of(BID4))
                    .gid(OGID)
                    .tag(Optional.empty())
                    .participantIdScore(
                            ImmutableMap.of(BID4, asList(6, 6),
                                    BID5, asList(0, 0)))
                    .build());

    @Test
    public void test() {
        final List<DecreasingDoubleScalarReason> result = sut.score(BASE::stream, BIDS_3_4_5, null,
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
                        .map(DecreasingDoubleScalarReason::getBid)
                        .collect(toList()),
                contains(BID5, BID3, BID4, BID2));
    }
}
