package org.dan.ping.pong.app.tournament;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.stream.Stream;

public class GroupMaxMapTest {
    @Test
    public void leaveMaxForEveryTag() {
        assertThat(GroupMaxMap.findMaxes(n -> n % 2,
                Integer::compare, Stream.of(0, 1, 2, 5, 6, 3, 4)),
                is(ImmutableMap.of(0, 6, 1, 5)));
    }
}
