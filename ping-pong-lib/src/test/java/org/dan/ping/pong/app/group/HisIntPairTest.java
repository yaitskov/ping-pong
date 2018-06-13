package org.dan.ping.pong.app.group;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class HisIntPairTest {
    private HisIntPair pair(int his, int enemy) {
        return HisIntPair.builder()
                .his(his)
                .enemy(enemy)
                .build();
    }

    @Test
    public void cmpWinWinLess() {
        assertThat(pair(2, 0).compareTo(pair(2, 1)), is(-1));
    }

    @Test
    public void cmpWinWinMore() {
        assertThat(pair(2, 1).compareTo(pair(2, 0)), is(1));
    }

    @Test
    public void cmpWinLostLess() {
        assertThat(pair(2, 0).compareTo(pair(0, 2)), is(-1));
    }

    @Test
    public void cmpLostWinMore() {
        assertThat(pair(1, 2).compareTo(pair(2, 0)), is(1));
    }

    @Test
    public void cmpLostEqMore() {
        assertThat(pair(1, 2).compareTo(pair(0, 0)), is(1));
    }

    @Test
    public void cmpWinEqLess() {
        assertThat(pair(2, 0).compareTo(pair(0, 0)), is(-1));
    }

    @Test
    public void cmpEqEqLess() {
        assertThat(pair(1, 1).compareTo(pair(0, 0)), is(-1));
    }

    @Test
    public void cmpEqEqMore() {
        assertThat(pair(0, 0).compareTo(pair(1, 1)), is(1));
    }

    @Test
    public void cmpEqEqEq() {
        assertThat(pair(1, 1).compareTo(pair(1, 1)), is(0));
    }

    @Test
    public void cmpEqWonMore() {
        assertThat(pair(1, 1).compareTo(pair(2, 0)), is(1));
    }

    @Test
    public void cmpEqLostLess() {
        assertThat(pair(1, 1).compareTo(pair(1, 2)), is(-1));
    }
}
