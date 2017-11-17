package org.dan.ping.pong.app.group;

import static org.dan.ping.pong.app.group.DisambiguationPolicy.WIN_AND_LOSE_COMPARATOR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import org.dan.ping.pong.app.bid.BidState;
import org.junit.Test;

public class DisambiguationPolicyTest {
    @Test
    public void moreCmpWinLostEqualsPunktsLostPlay() {
        final BidSuccessInGroup weaker = BidSuccessInGroup.builder()
                .finalState(BidState.Lost)
                .punkts(2)
                .winSets(0)
                .lostSets(6)
                .winBalls(9)
                .lostBalls(66)
                .build();
        final BidSuccessInGroup stronger = BidSuccessInGroup.builder()
                .finalState(BidState.Play)
                .punkts(2)
                .winSets(3)
                .lostBalls(0)
                .winBalls(33)
                .lostBalls(3)
                .build();
        assertThat(WIN_AND_LOSE_COMPARATOR.compare(stronger, weaker), lessThan(0));
    }
}
