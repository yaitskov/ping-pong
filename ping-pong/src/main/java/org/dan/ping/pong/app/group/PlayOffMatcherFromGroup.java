package org.dan.ping.pong.app.group;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.primitives.Ints.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PlayOffMatcherFromGroup {
    private static final Map<Integer, Map<Integer, Map<Integer, List<Integer>>>> MAP = generate(1, 129);

    public static Map<Integer, List<Integer>> generateForQuits1(int groups) {
        if (groups == 1) {
            return Collections.emptyMap();
        }
        checkArgument(groups % 2 == 0);
        final Map<Integer, List<Integer>> result = new HashMap<>();
        int ig = 0;
        for (; ig < groups / 2; ++ig) {
            result.put(ig, singletonList(ig ));
        }
        for (; ig < groups; ++ig) {
            result.put(ig, singletonList(groups - ig - 1));
        }
        return result;
    }

    public static Map<Integer, List<Integer>> generateForQuits2(int groups) {
        if (groups == 1) {
            return ImmutableMap.of(0, asList(0, 0));
        }
        checkArgument(groups % 2 == 0);
        final Map<Integer, List<Integer>> result = new HashMap<>();
        int a = 0;
        int b = groups - 1;
        int tmp;
        for (int ig = 0; ig < groups; ig += 2) {
            result.put(ig, asList(a, b));
            result.put(ig + 1, asList(b, a));
            ++a;
            --b;
            tmp = a;
            a = b;
            b = tmp;
        }
        return result;
    }

    public static Map<Integer, Map<Integer, Map<Integer, List<Integer>>>> generate(int minGroups, int maxGroups) {
        final Map<Integer, Map<Integer, Map<Integer, List<Integer>>>> result = new HashMap<>();
        for (int igroups = minGroups; igroups < maxGroups; igroups *= 2) {
            final Map<Integer, Map<Integer, List<Integer>>> subResult = new HashMap<>();
            int[] i = new int[1];
            Stream.of(Pair.of(1, generateForQuits1(igroups)),
                    Pair.of(2, generateForQuits2(igroups)))
                    .filter(o -> !o.getValue().isEmpty())
                    .forEach(o -> subResult.put(o.getKey(), o.getValue()));
            if (subResult.isEmpty()) {
                continue;
            }
            result.put(igroups, subResult);
        }
        return result;
    }

    public static List<Integer> find(int quits, int groupOrdNumber, int groups) {
        return ofNullable(MAP.get(groups))
                .map(m -> ofNullable(m.get(quits))
                        .map(m2 -> ofNullable(m2.get(groupOrdNumber))
                                .orElseThrow(() -> internalError("No mapping for "
                                        + groups + ":" + quits
                                        + ":" + groupOrdNumber
                                        + " groups:quits:groupNumber")))
                        .orElseThrow(() -> internalError("No mapping for "
                                + groups + ":" + quits + " groups:quits"))
                )
                .orElseThrow(() -> internalError("No mapping for " + groups + " groups"));
    }
}
