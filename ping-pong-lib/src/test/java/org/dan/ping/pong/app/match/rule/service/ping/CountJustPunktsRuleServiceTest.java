package org.dan.ping.pong.app.match.rule.service.ping;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.BID2;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID4;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.Punkts;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofIntD;
import static org.dan.ping.pong.app.match.rule.service.common.BallsBalanceRuleServiceTest.MATCHES_UIDS_2_3_4_S;
import static org.dan.ping.pong.app.match.rule.service.common.BallsBalanceRuleServiceTest.UIDS_2_3_4;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.junit.Test;

public class CountJustPunktsRuleServiceTest {
    CountJustPunktsRuleService sut = new CountJustPunktsRuleService();

    @Test
    public void countPunkts() {
        assertThat(
                sut.score(MATCHES_UIDS_2_3_4_S, UIDS_2_3_4, null,
                        GroupRuleParams.builder()
                                .tournament(TournamentMemState.builder()
                                        .sport(SportType.Tennis)
                                        .build())
                                .build())
                        .get().collect(toList()),
                is(asList(ofIntD(BID3, 4, Punkts),
                        ofIntD(BID4, 3, Punkts),
                        ofIntD(BID2, 1, Punkts))));
    }
}
