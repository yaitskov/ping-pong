package org.dan.ping.pong.util.collection;

import static com.google.common.primitives.Ints.asList;
import static java.util.Collections.emptyList;
import static org.dan.ping.pong.util.collection.IterableComparator.LengthPolicy.LongerSmaller;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class IterableComparatorTest {
    private IterableComparator<Integer> sut = new IterableComparator<>(Integer::compare);

    @Test
    public void compareCollectionWithItSelfGets0() {
        assertThat(
                sut.compare(emptyList(), emptyList(), LongerSmaller),
                is(0));

        assertThat(
                sut.compare(asList(1, 2), asList(1, 2), LongerSmaller),
                is(0));
    }

    @Test
    public void compareEqLenButMore() {
        assertThat(
                sut.compare(asList(1, 3), asList(1, 2), LongerSmaller),
                is(1));
    }

    @Test
    public void compareEqLenButLess() {
        assertThat(
                sut.compare(asList(1, 3), asList(2, 1), LongerSmaller),
                is(-1));
    }

    @Test
    public void compareLessDueLonger() {
        assertThat(
                sut.compare(asList(1, 3), asList(1), LongerSmaller),
                is(-1));
    }

    @Test
    public void compareBiggerDueShorter() {
        assertThat(
                sut.compare(asList(1), asList(1, 2), LongerSmaller),
                is(1));
    }
}
