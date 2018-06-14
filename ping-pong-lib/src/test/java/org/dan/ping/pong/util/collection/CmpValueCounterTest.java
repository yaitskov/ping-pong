package org.dan.ping.pong.util.collection;

import static org.dan.ping.pong.app.match.rule.service.common.WeightedMatchesRuleService.CMP_VALUE_COUNTER_COMPARATOR;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.app.group.HisIntPair;
import org.junit.Test;

public class CmpValueCounterTest {
    private static final HisIntPair a = new HisIntPair(3, 0);
    private static final HisIntPair b = new HisIntPair(2, 0);
    private static final HisIntPair c = new HisIntPair(1, 0);

    @Test
    public void cmpSameMoreDueRepeatsLess() {
        assertThat(CMP_VALUE_COUNTER_COMPARATOR.compare(
                create(a, 2), create(a, 3)), is(1));
    }

    @Test
    public void cmpSameLessDueRepeatsMore() {
        assertThat(
                CMP_VALUE_COUNTER_COMPARATOR.compare(
                        create(a, 4), create(a, 3)), is(-1));
    }

    @Test
    public void cmpMoreDueValueMore() {
        assertThat(CMP_VALUE_COUNTER_COMPARATOR.compare(
                create(b, 0), create(a, 3)), is(1));
    }

    @Test
    public void cmpLessDueValueLess() {
        assertThat(CMP_VALUE_COUNTER_COMPARATOR.compare(
                create(b, 1), create(c, 1)), is(-1));
    }

    CmpValueCounter<HisIntPair> create(HisIntPair value, int repeats) {
        return new CmpValueCounter<>(value, repeats);
    }
}
