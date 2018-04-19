package org.dan.ping.pong.app.match.rule.reason;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostBalls;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonSets;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofIntD;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingLongScalarReason.ofLongD;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DecreasingLongScalarReasonTest {
    @Test
    public void equals() {
        assertThat(ofLongD(1, WonSets), is(ofLongD(1, WonSets)));
    }

    @Test
    public void notEqualsValue() {
        assertThat(ofLongD(2, WonSets), not(is(ofLongD(1, WonSets))));
    }

    @Test
    public void notEqualsRule() {
        assertThat(ofLongD(1, LostBalls), not(is(ofLongD(1, WonSets))));
    }
}
