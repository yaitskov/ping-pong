package org.dan.ping.pong.app.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.dan.ping.pong.sys.type.number.ImmutableNumber;

public class Mid extends ImmutableNumber {
    @JsonCreator
    public Mid(int id) {
        super(id);
    }

    // jax-rsp
    @JsonCreator
    public static Mid valueOf(String s) {
        return new Mid(Integer.valueOf(s));
    }

    public static Mid of(int id) {
        return new Mid(id);
    }
}
