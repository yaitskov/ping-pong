package org.dan.ping.pong.util.matcher;

import static java.util.UUID.randomUUID;
import static org.dan.ping.pong.util.matcher.UuidMatcher.isUuid;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class UuidMatcherTest {
    @Test
    public void pass() {
        assertThat(randomUUID().toString(), isUuid());
    }

    @Test(expected = AssertionError.class)
    public void fail() {
        assertThat("12345-33", isUuid());
    }
}
