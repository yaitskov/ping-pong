package org.dan.ping.pong.util.collection;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class MapUtils {
    public static <T> Map<Integer, T> makeKeysSequential(Map<Integer, T> m) {
        final List<Integer> keys = m.keySet().stream().sorted().collect(toList());
        if (m.isEmpty()) {
            return m;
        }
        int previousKey = keys.get(0) - 1;
        for (int key : keys) {
            int diff = key - previousKey - 1;
            previousKey = key - diff;
            m.put(previousKey, m.remove(key));
        }
        return m;
    }

    public static <K, V> Map<K, V> merge(Map<K, V> a, Map<K, V> b, BiFunction<V, V, V> merger) {
        return Stream.of(a, b).flatMap(m -> m.entrySet().stream())
                .collect(
                        toMap(Map.Entry::getKey,
                                Map.Entry::getValue,
                                merger::apply));

    }
}
