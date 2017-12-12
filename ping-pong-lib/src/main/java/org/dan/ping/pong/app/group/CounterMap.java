package org.dan.ping.pong.app.group;

import java.util.HashMap;
import java.util.Map;

public class CounterMap<T> {
    private final Map<T, Integer> map;

    public CounterMap() {
        map = new HashMap<>();
    }

    public void increment(T key) {
        map.merge(key, 1, (o, n) -> o + n);
    }

    public Map<T, Integer> toMap() {
        return map;
    }
}
