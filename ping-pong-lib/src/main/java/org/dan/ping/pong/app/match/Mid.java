package org.dan.ping.pong.app.match;

import static java.lang.Integer.compare;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Mid implements Comparable<Mid> {
    @Getter(onMethod = @__(@JsonValue))
    private final int id;

    // jax-rsp
    @JsonCreator
    public static Mid valueOf(String s) {
        return new Mid(Integer.valueOf(s));
    }

    public static Mid of(int id) {
        return new Mid(id);
    }

    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public int compareTo(Mid o) {
        return compare(id, o.id);
    }

    public Mid negate() {
        return new Mid(-id);
    }
}
