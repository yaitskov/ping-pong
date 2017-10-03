package org.dan.ping.pong.app.group;

import static com.google.common.primitives.Ints.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import org.dan.ping.pong.sys.error.PiPoEx;
import org.junit.Test;

public class GroupSizeCalculatorTest {
    private GroupSizeCalculator sut = new GroupSizeCalculator();
    private GroupRules rules = GroupRules.builder()
            .groupSize(5)
            .quits(1)
            .build();

    @Test(expected = PiPoEx.class)
    public void split1Bid() {
        sut.calcGroupSizes(rules, 1);
    }

    @Test
    public void split2Bids() {
        assertEquals(singletonList(2), sut.calcGroupSizes(rules, 2));
    }

    @Test
    public void split4Bids() {
        assertEquals(singletonList(4), sut.calcGroupSizes(rules, 4));
    }

    @Test
    public void split5Bids() {
        assertEquals(singletonList(5), sut.calcGroupSizes(rules, 5));
    }

    @Test
    public void split6Bids() {
        assertEquals(asList(3, 3), sut.calcGroupSizes(rules, 6));
    }

    @Test
    public void split7Bids() {
        assertEquals(asList(3, 4), sut.calcGroupSizes(rules, 7));
    }

    @Test
    public void split8Bids() {
        assertEquals(asList(4, 4), sut.calcGroupSizes(rules, 8));
    }

    @Test
    public void split9Bids() {
        assertEquals(asList(5, 4), sut.calcGroupSizes(rules, 9));
    }

    @Test
    public void split10Bids() {
        assertEquals(asList(5, 5), sut.calcGroupSizes(rules, 10));
    }

    @Test
    public void split11Bids() {
        assertEquals(asList(3, 3, 3, 2), sut.calcGroupSizes(rules, 11));
    }

    @Test
    public void split12Bids() {
        assertEquals(asList(3, 3, 3, 3), sut.calcGroupSizes(rules, 12));
    }

    @Test
    public void split13Bids() {
        assertEquals(asList(3, 3, 3, 4), sut.calcGroupSizes(rules, 13));
    }

    @Test
    public void split16Bids() {
        assertEquals(asList(4, 4, 4, 4), sut.calcGroupSizes(rules, 16));
    }

    @Test
    public void split17Bids() {
        assertEquals(asList(5, 4, 4, 4), sut.calcGroupSizes(rules, 17));
    }

    @Test
    public void split18Bids() {
        assertEquals(asList(5, 5, 4, 4), sut.calcGroupSizes(rules, 18));
    }

    @Test
    public void split24Bids() {
        assertEquals(asList(3, 3, 3, 3, 3, 3, 3, 3),
                sut.calcGroupSizes(rules, 24));
    }
}
