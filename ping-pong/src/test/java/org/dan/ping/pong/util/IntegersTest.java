package org.dan.ping.pong.util;

import static org.dan.ping.pong.util.Integers.odd;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IntegersTest {
    @Test
    public void oddTrue() {
        assertTrue(odd(1));
    }
    @Test
    public void oddFalse() {
        assertFalse(odd(0));
        assertFalse(odd(2));
    }
}
