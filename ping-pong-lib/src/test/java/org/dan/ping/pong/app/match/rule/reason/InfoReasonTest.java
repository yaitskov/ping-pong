package org.dan.ping.pong.app.match.rule.reason;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostSets;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonSets;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class InfoReasonTest {
    @Test
    public void equals() {
        assertThat(new InfoReason(WonSets), is(new InfoReason(WonSets)));
    }

    @Test
    public void notEquals() {
        assertThat(new InfoReason(WonSets), not(is(new InfoReason(LostSets))));
    }
}
