package org.dan.ping.pong.app.tournament.marshaling;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class StrictMapper<A, B> implements Function<A, B> {
    private final String name;
    private final Map<A, B> map;

    @Override
    public B apply(A a) {
        return ofNullable(map.get(a))
                .orElseThrow(() -> internalError(
                        "No mapping for ["
                                + a + "] in [" + name + "]"));
    }
}
