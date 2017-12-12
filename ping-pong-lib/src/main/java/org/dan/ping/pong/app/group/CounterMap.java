package org.dan.ping.pong.app.group;

import org.dan.ping.pong.app.bid.Uid;

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

    public void zeroIfMissing(T uid) {
        map.putIfAbsent(uid, 0);
    }
}
