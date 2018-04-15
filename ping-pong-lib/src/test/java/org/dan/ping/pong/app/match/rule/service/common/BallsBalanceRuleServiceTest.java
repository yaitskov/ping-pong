package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.UID2;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID4;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.BallsBalance;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofIntD;
import static org.dan.ping.pong.app.match.rule.service.common.PickRandomlyRuleServiceTest.FAILING_SUPPLIER;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.UidsProvider;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BallsBalanceRuleServiceTest {
    public static final List<MatchInfo> MATCHES_UIDS_2_3_4 = asList(
            MatchInfo.builder()
                    .participantIdScore(
                            ImmutableMap.of(UID2, asList(0, 0),
                                    UID3, asList(6, 6)))
                    .build(),
            MatchInfo.builder()
                    .participantIdScore(
                            ImmutableMap.of(UID2, asList(0, 0),
                                    UID4, asList(6, 6)))
                    .build(),
            MatchInfo.builder()
                    .participantIdScore(
                            ImmutableMap.of(UID4, asList(3, 2),
                                    UID3, asList(6, 6)))
                    .build());

    public static final Supplier<Stream<MatchInfo>> MATCHES_UIDS_2_3_4_S = MATCHES_UIDS_2_3_4::stream;

    public static final UidsProvider UIDS_2_3_4 = new UidsProvider(
            ImmutableSet.of(UID2, UID3, UID4), FAILING_SUPPLIER);

    private BallsBalanceRuleService sut = new BallsBalanceRuleService();

    @Test
    public void ballBalanceBoth() {
        assertThat(
                sut.score(MATCHES_UIDS_2_3_4_S, UIDS_2_3_4, null, null)
                        .get().collect(toList()),
                is(asList(ofIntD(UID3, 19, BallsBalance),
                        ofIntD(UID4, 5,BallsBalance),
                        ofIntD(UID2, -24, BallsBalance))));
    }
}
