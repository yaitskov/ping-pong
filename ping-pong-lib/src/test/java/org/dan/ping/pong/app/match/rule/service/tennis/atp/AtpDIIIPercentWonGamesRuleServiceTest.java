package org.dan.ping.pong.app.match.rule.service.tennis.atp;

import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.UID2;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID4;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID5;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpDIII;
import static org.dan.ping.pong.app.match.rule.service.tennis.atp.AtpDIIPercentWonSetsRuleTest.BASE;
import static org.dan.ping.pong.app.match.rule.service.tennis.atp.AtpDIRuleServiceTest.UIDS_3_4_5;
import static org.dan.ping.pong.app.sport.tennis.TennisSportTest.CLASSIC_TENNIS_RULES;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.Every.everyItem;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.app.match.rule.reason.DecreasingDoubleScalarReason;
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

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        AtpDIIIPercentWonGamesRuleService.class,
        SportCtx.class})
public class AtpDIIIPercentWonGamesRuleServiceTest {
    @Inject
    private AtpDIIIPercentWonGamesRuleService sut;

    @Test
    public void test() {
        final List<DecreasingDoubleScalarReason> result = sut.score(
                BASE::stream, UIDS_3_4_5, null,
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
                        .map(DecreasingDoubleScalarReason::getRule)
                        .collect(toList()),
                everyItem(is(AtpDIII)));
        assertThat(
                result.stream()
                        .map(DecreasingDoubleScalarReason::getUid)
                        .collect(toList()),
                contains(UID3, UID5, UID4, UID2));
        assertThat(
                result.stream()
                        .map(DecreasingDoubleScalarReason::getValue)
                        .collect(toList()),
                contains(closeTo(29.0 / 41.0, 1e-5),
                        closeTo(24.0 / 41.0, 1e-5),
                        closeTo(20.0 / 38.0, 1e-5),
                        closeTo(6.0 / 37.0, 1e-5)));
    }
}
