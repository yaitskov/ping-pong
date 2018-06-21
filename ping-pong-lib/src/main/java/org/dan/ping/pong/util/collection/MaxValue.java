package org.dan.ping.pong.util.collection;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public class MaxValue<T extends Comparable> implements Consumer<T>, Function<T, T> {
    private T max;

    @Override
    public void accept(T t) {
        if (max.compareTo(t) < 0) {
            max = t;
        }
    }

    @Override
    public T apply(T t) {
        accept(t);
        return t;
    }
}
