package org.dan.ping.pong.util;

import java.util.Optional;
import java.util.function.BiFunction;

public class OptPlus {
    public static <A, B, C> Optional<C> oMap2(
            BiFunction<A,B,C> f,
            Optional<A> oA,
            Optional<B> oB) {
        return oA.flatMap((a) -> oB.map((b) -> f.apply(a, b)));
    }
}
