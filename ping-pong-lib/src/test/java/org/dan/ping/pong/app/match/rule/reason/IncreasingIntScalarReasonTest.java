package org.dan.ping.pong.app.match.rule.reason;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostBalls;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonSets;
import static org.dan.ping.pong.app.match.rule.reason.IncreasingIntScalarReason.ofIntI;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class IncreasingIntScalarReasonTest {
    @Test
    public void equals() {
        assertThat(ofIntI(1, WonSets), is(ofIntI(1, WonSets)));
    }

    @Test
    public void notEqualsValue() {
        assertThat(ofIntI(2, WonSets), not(is(ofIntI(1, WonSets))));
    }

    @Test
    public void notEqualsRule() {
        assertThat(ofIntI(1, LostBalls), not(is(ofIntI(1, WonSets))));
    }
}
