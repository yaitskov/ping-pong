package org.dan.ping.pong.app.bid;

import static java.lang.Integer.compare;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Uid implements Comparable<Uid> {
    @Getter(onMethod = @__(@JsonValue))
    private final int id;

    // jax-rsp
    @JsonCreator
    public static Uid valueOf(String s) {
        return new Uid(Integer.valueOf(s));
    }

    @Override
    public int compareTo(Uid o) {
        return compare(id, o.id);
    }
}
