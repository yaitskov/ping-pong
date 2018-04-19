package org.dan.ping.pong.app.match.rule.reason;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostBalls;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonSets;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofIntD;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DecreasingIntScalarReasonTest {
    @Test
    public void equals() {
        assertThat(ofIntD(1, WonSets), is(ofIntD(1, WonSets)));
    }

    @Test
    public void notEqualsValue() {
        assertThat(ofIntD(2, WonSets), not(is(ofIntD(1, WonSets))));
    }

    @Test
    public void notEqualsRule() {
        assertThat(ofIntD(1, LostBalls), not(is(ofIntD(1, WonSets))));
    }
}
