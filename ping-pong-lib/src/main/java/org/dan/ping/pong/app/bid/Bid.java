package org.dan.ping.pong.app.bid;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.dan.ping.pong.sys.type.number.ImmutableNumber;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class Bid extends ImmutableNumber {
    @JsonCreator
    public Bid(int id) {
        super(id);
    }

    // jax-rsp
    @JsonCreator
    public static Bid valueOf(String s) {
        return new Bid(Integer.valueOf(s));
    }

    public static Bid of(int id) {
        return new Bid(id);
    }

    @Min(2)
    @Max(Short.MAX_VALUE)
    public int getValidateValue() {
        return super.getValidateValue();
    }
}
