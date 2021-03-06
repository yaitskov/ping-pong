package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupServiceTest.BID2;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID4;
import static org.dan.ping.pong.app.match.rule.service.common.PickRandomlyRuleServiceTest.FAILING_SUPPLIER;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.reason.F2fReason;
import org.dan.ping.pong.sys.error.PiPoEx;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.Stream;

public class DirectOutcomeRuleServiceTest {
    private DirectOutcomeRuleService sut = new DirectOutcomeRuleService();

    @Test
    public void skipGroupWithMoreThat2Uids() {
        assertThat(
                sut.score(FAILING_SUPPLIER,
                        ImmutableSet.of(BID2, BID3, BID4),
                        null, null),
                is(Optional.empty()));
    }

    @Test(expected = PiPoEx.class)
    public void errorIfMoreThat1Match() {
        sut.score(() -> Stream.of(new MatchInfo(), new MatchInfo()),
                ImmutableSet.of(BID2, BID3),
                null, null);
    }

    @Test
    public void skipOnIncompleteMatch() {
        assertThat(
                sut.score(() -> Stream.of(MatchInfo.builder()
                                .winnerId(Optional.empty())
                                .build()),
                        ImmutableSet.of(BID2, BID3),
                        null, null),
                is(Optional.empty()));
    }

    @Test
    public void onCompleteMatch() {
        assertThat(
                sut.score(() -> Stream.of(MatchInfo.builder()
                                .winnerId(Optional.of(BID2))
                                .participantIdScore(ImmutableMap.of(
                                        BID2, emptyList(),
                                        BID3, emptyList()))
                                .build()),
                        ImmutableSet.of(BID2, BID3),
                        null, null).get().collect(toList()),
                is(asList(F2fReason.builder()
                                .won(1)
                                .bid(BID2)
                                .opponentBid(BID3)
                                .build(),
                        F2fReason.builder()
                                .bid(BID3)
                                .opponentBid(BID2)
                                .build())));
    }
}
