package org.dan.ping.pong.app.category;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.dan.ping.pong.sys.type.number.ImmutableNumber;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class Cid extends ImmutableNumber {
    @JsonCreator
    public Cid(int id) {
        super(id);
    }

    // jax-rsp
    @JsonCreator
    public static Cid valueOf(String s) {
        return new Cid(Integer.valueOf(s));
    }

    public static Cid of(int id) {
        return new Cid(id);
    }

    @Min(1)
    @Max(Short.MAX_VALUE)
    public int getValidateValue() {
        return super.getValidateValue();
    }
}
