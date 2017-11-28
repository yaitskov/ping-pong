package org.dan.ping.pong.app.group;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.group.PlayOffMatcherFromGroup.generateForQuits2;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class PlayOffMatcherFromGroupTest {
    @Test
    public void generateForQuits2For2Groups() {
         Map<Integer, List<Integer>> m = generateForQuits2(2);
         assertEquals(ImmutableMap.of(0, asList(0, 1), 1, asList(1, 0)), m);
    }
}
