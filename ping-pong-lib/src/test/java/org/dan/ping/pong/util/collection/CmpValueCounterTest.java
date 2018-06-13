package org.dan.ping.pong.util.collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CmpValueCounterTest {
    @Test
    public void cmpSameMoreDueRepeatsLess() {
        assertThat(create("a", 2).compareTo(create("a", 3)), is(1));
    }

    @Test
    public void cmpSameLessDueRepeatsMore() {
        assertThat(create("a", 4).compareTo(create("a", 3)), is(-1));
    }

    @Test
    public void cmpMoreDueValueMore() {
        assertThat(create("b", 0).compareTo(create("a", 3)), is(1));
    }

    @Test
    public void cmpLessDueValueLess() {
        assertThat(create("b", 1).compareTo(create("c", 1)), is(-1));
    }

    CmpValueCounter<String> create(String value, int repeats) {
        return new CmpValueCounter<>(value, repeats);
    }
}
