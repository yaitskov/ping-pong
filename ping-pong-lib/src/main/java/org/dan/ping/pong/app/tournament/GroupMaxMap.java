package org.dan.ping.pong.app.tournament;

import static java.util.stream.Collectors.toMap;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public interface GroupMaxMap {
     static <K, V> Map<K, V> findMaxes(Function<V, K> keyF,
             Comparator<V> pickGreater, Stream<V> s) {
         return s.collect(toMap(keyF, o -> o,
                (a, b) -> pickGreater.compare(a, b) > 0 ? a : b));

    }
}
