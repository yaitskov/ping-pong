package org.dan.ping.pong.app.server.castinglots;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.primitives.Ints.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class GroupDividerTest {
    private GroupDivider sut = new GroupDivider();

    @Test
    public void splitBalanced12() {
        check(of(0, asList(0), 1, asList(1, 2)), asList(1, 2));
    }

    @Test
    public void splitBalanced22() {
        check(of(0, asList(0, 3), 1, asList(1, 2)), asList(2, 2));
    }

    @Test
    public void splitBalanced32() {
        check(of(0, asList(0, 3, 4), 1, asList(1, 2)), asList(3, 2));
    }

    @Test
    public void splitBalanced33() {
        check(of(0, asList(0, 3, 4), 1, asList(1, 2, 5)), asList(3, 3));
    }

    @Test
    public void splitBalanced212() {
        check(of(0, asList(0, 4),
                1, asList(1),
                2, asList(2, 3)), asList(2, 1, 2));
    }

    @Test
    public void splitBalanced121() {
        check(of(0, asList(0), 1, asList(1, 3), 2, asList(2)),
                asList(1, 2, 1));
    }

    private void check(Map<Integer, List<Integer>> groupMembers, List<Integer> groups) {
        Map<Integer, List<Integer>> result = new HashMap<>();
        List<Integer> bids = l(groups.stream().mapToInt(x -> x).sum());
        sut.balancedMix(bids, result, groups);
        assertEquals(groupMembers, result);
        assertEquals(groups.size(), result.size());
        assertEquals(emptyList(), bids);
    }

    private List<Integer> l(int n) {
        return IntStream.range(0, n).mapToObj(x -> x).collect(toList());
    }
}
