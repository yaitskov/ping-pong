package org.dan.ping.pong.app.place;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
public class Pid {
    @Getter(onMethod = @__(@JsonValue))
    private final int pid;

    // jax-rsp
    @JsonCreator
    public static Pid valueOf(String s) {
        return new Pid(Integer.valueOf(s));
    }

    public String toString() {
        return "pid(" + pid + ")";
    }
}
