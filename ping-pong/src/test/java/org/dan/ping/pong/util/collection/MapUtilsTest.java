package org.dan.ping.pong.util.collection;

import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.util.collection.MapUtils.makeKeysSequential;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.stream.Stream;

public class MapUtilsTest {
    @Test
    public void compact135() {
        assertThat(
                makeKeysSequential(Stream.of(1, 3, 5).collect(toMap(o -> o, o -> o))),
                is(ImmutableMap.of(1, 1, 2, 3, 3, 5)));
    }

}
