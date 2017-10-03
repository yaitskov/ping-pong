package org.dan.ping.pong.app.server.group;

import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GroupSizeCalculator {
    public static final String GROUP_GETS_LESS_THAN_2_PARTICIPANT = "group gets less than 2 participant";

    private static final TreeMap<Integer, Integer> N2POWER = new TreeMap<>(
            ImmutableMap.<Integer, Integer>builder()
                    .put(1, 0)
                    .put(2, 1)
                    .put(4, 2)
                    .put(8, 3)
                    .put(16, 4)
                    .put(32, 5)
                    .put(64, 6)
                    .put(128, 7)
                    .put(256, 8)
                    .build());

    public List<Integer> calcGroupSizes(GroupRules rules, int bidsInCategory) {
        final int incompleteGroupSize = bidsInCategory % rules.getGroupSize();
        final int groups = bidsInCategory / rules.getGroupSize()
                + (incompleteGroupSize > 0 ? 1 : 0);
        final int normalizedGroups = N2POWER.containsKey(groups)
                ? groups
                : N2POWER.higherKey(groups);

        final int groupSize = bidsInCategory / normalizedGroups;
        if (groupSize < 2) {
            throw badRequest(GROUP_GETS_LESS_THAN_2_PARTICIPANT);
        }
        final List<Integer> result = IntStream.range(0, normalizedGroups)
                .mapToObj(i -> groupSize)
                .collect(Collectors.toList());
        final int rest = bidsInCategory - groupSize * normalizedGroups;
        fill(rest, result, groupSize % 2 == 0
                ? 0
                : (normalizedGroups - 1));
        return result;
    }

    private void fill(int bidsToDistribute, List<Integer> result, int start) {
        int j = start;
        int inc = start == 0 ? 1 : -1;
        for (int i = 0; i < bidsToDistribute; ++i) {
            result.set(j, 1 + result.get(j));
            j += inc;
            if (j == result.size()) {
                j = result.size() -1;
                inc = -1;
            } else if (j < 0) {
                j = 0;
                inc = 1;
            }
        }
    }
}
