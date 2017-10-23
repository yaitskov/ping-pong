package org.dan.ping.pong.app.tournament;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Tid {
    @Getter(onMethod = @__(@JsonValue))
    private final int tid;

    // jax-rsp
    @JsonCreator
    public static Tid valueOf(String s) {
        return new Tid(Integer.valueOf(s));
    }

    public static Tid of(int id) {
        return new Tid(id);
    }

    public String toString() {
        return String.valueOf(tid);
    }
}
