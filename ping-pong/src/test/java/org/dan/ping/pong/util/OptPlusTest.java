package org.dan.ping.pong.util;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.dan.ping.pong.util.OptPlus.oMap2;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.Optional;

public class OptPlusTest {
    final Optional<Integer> empty = Optional.empty();

    @Test
    public void oMap2EmptyWhenFirstArgEmpty() {
        assertThat(oMap2((a, b) -> a + b, empty, of(1)), is(empty()));
    }

    @Test
    public void oMap2EmptyWhenSecondArgEmpty() {
        assertThat(oMap2((a, b) -> a + b, of(1), empty), is(empty()));
    }

    @Test
    public void oMap2EmptyWhenBothArgEmpty() {
        assertThat(oMap2((a, b) -> a + b, empty, empty), is(empty()));
    }

    @Test
    public void oMap2ApplyWhenBothArgNonEmpty() {
        assertThat(oMap2((a, b) -> a + b, of(1), of(2)), is(of(3)));
    }
}
