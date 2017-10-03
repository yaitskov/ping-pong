package org.dan.ping.pong.app.server.tournament;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class Tid {
    private final int tid;

    // jax-rs
    public static Tid valueOf(String s) {
        return new Tid(Integer.valueOf(s));
    }

    public String toString() {
        return String.valueOf(tid);
    }
}
