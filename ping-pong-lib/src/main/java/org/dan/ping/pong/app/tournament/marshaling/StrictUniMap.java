package org.dan.ping.pong.app.tournament.marshaling;

import java.util.Map;

public class StrictUniMap<A> extends StrictMapper<A, A> {
    public StrictUniMap(String name, Map<A, A> map) {
        super(name, map);
    }

    public static <T> StrictUniMap<T> of(String name, Map<T, T> map) {
        return new StrictUniMap<T>(name, map);
    }
}
