package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.UID2;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID4;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostSets;
import static org.dan.ping.pong.app.match.rule.reason.IncreasingIntScalarReason.ofIntD;
import static org.dan.ping.pong.app.match.rule.service.common.BallsBalanceRuleServiceTest.MATCHES_UIDS_2_3_4_S;
import static org.dan.ping.pong.app.match.rule.service.common.BallsBalanceRuleServiceTest.UIDS_2_3_4;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.sport.SportCtx;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LostSetsRuleService.class, SportCtx.class})
public class LostSetsRuleServiceTest {
    @Inject
    private LostSetsRuleService sut;

    @Test
    public void setsBalance() {
        assertThat(
                sut.score(MATCHES_UIDS_2_3_4_S, UIDS_2_3_4, null,
                        GroupRuleParams.builder()
                                .tournament(TournamentMemState.builder()
                                        .sport(SportType.Tennis)
                                        .build())
                                .build())
                        .get().collect(toList()),
                is(asList(ofIntD(UID3, 0, LostSets),
                        ofIntD(UID4, 2, LostSets),
                        ofIntD(UID2, 4, LostSets))));
    }
}
