package org.dan.ping.pong.app.match.dispute;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.dan.ping.pong.sys.type.number.ImmutableNumber;

public class DisputeId extends ImmutableNumber {
    @JsonCreator
    public DisputeId(int v) {
        super(v);
    }

    // jax-rsp
    @JsonCreator
    public static DisputeId valueOf(String s) {
        return new DisputeId(Integer.valueOf(s));
    }

    public static DisputeId of(int id) {
        return new DisputeId(id);
    }
}
