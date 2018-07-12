package org.dan.ping.pong.app.group;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.dan.ping.pong.sys.type.number.ImmutableNumber;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class Gid extends ImmutableNumber {
    @JsonCreator
    public Gid(int id) {
        super(id);
    }

    // jax-rsp
    @JsonCreator
    public static Gid valueOf(String s) {
        return new Gid(Integer.valueOf(s));
    }

    public static Gid of(int id) {
        return new Gid(id);
    }

    @Min(0)
    @Max(Short.MAX_VALUE)
    public int getValidateValue() {
        return super.getValidateValue();
    }
}
