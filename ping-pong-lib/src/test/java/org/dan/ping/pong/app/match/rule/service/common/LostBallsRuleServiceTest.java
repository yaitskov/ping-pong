package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.UID2;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID4;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostBalls;
import static org.dan.ping.pong.app.match.rule.reason.IncreasingIntScalarReason.ofIntD;
import static org.dan.ping.pong.app.match.rule.service.common.BallsBalanceRuleServiceTest.MATCHES_UIDS_2_3_4_S;
import static org.dan.ping.pong.app.match.rule.service.common.BallsBalanceRuleServiceTest.UIDS_2_3_4;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class LostBallsRuleServiceTest {
    private LostBallsRuleService sut = new LostBallsRuleService();

    @Test
    public void ballBalanceBoth() {
        assertThat(
                sut.score(MATCHES_UIDS_2_3_4_S, UIDS_2_3_4, null, null)
                        .get().collect(toList()),
                is(asList(ofIntD(UID3, 6, LostBalls),
                        ofIntD(UID4, 12, LostBalls),
                        ofIntD(UID2, 24, LostBalls))));
    }
}
