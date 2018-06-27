package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.BID2;
import static org.dan.ping.pong.app.match.MatchState.Break;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID4;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.BallsBalance;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofIntD;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BallsBalanceRuleServiceTest {
    public static final List<MatchInfo> MATCHES_UIDS_2_3_4 = asList(
            MatchInfo.builder()
                    .winnerId(Optional.of(BID3))
                    .state(Over)
                    .participantIdScore(
                            ImmutableMap.of(BID2, asList(0, 0),
                                    BID3, asList(6, 6)))
                    .build(),
            MatchInfo.builder()
                    .state(Break)
                    .winnerId(Optional.of(BID4))
                    .participantIdScore(
                            ImmutableMap.of(BID2, asList(0, 0),
                                    BID4, asList(6, 5)))
                    .build(),
            MatchInfo.builder()
                    .winnerId(Optional.of(BID3))
                    .state(Over)
                    .participantIdScore(
                            ImmutableMap.of(BID4, asList(3, 2),
                                    BID3, asList(6, 6)))
                    .build());

    public static final Supplier<Stream<MatchInfo>> MATCHES_UIDS_2_3_4_S = MATCHES_UIDS_2_3_4::stream;

    public static final Set<Bid> UIDS_2_3_4 = ImmutableSet.of(BID2, BID3, BID4);

    private BallsBalanceRuleService sut = new BallsBalanceRuleService();

    @Test
    public void ballBalanceBoth() {
        assertThat(
                sut.score(MATCHES_UIDS_2_3_4_S, UIDS_2_3_4, null, null)
                        .get().collect(toList()),
                is(asList(ofIntD(BID3, 19, BallsBalance),
                        ofIntD(BID4, 4,BallsBalance),
                        ofIntD(BID2, -23, BallsBalance))));
    }
}
