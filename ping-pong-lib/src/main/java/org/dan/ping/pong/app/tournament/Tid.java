package org.dan.ping.pong.app.tournament;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.dan.ping.pong.sys.type.number.ImmutableNumber;

import javax.validation.constraints.Min;

public class Tid extends ImmutableNumber {
    public static final String TOURNAMENT_ID_SHOULD_BE_A_POSITIVE_NUMBER = "tournament id should be a positive number";

    @JsonCreator
    public Tid(int value) {
        super(value);
    }

    @Min(value = 1, message = TOURNAMENT_ID_SHOULD_BE_A_POSITIVE_NUMBER)
    public int getValidateValue() {
        return super.getValidateValue();
    }

    // jax-rsp
    @JsonCreator
    public static Tid valueOf(String s) {
        return new Tid(Integer.valueOf(s));
    }

    public static Tid of(int id) {
        return new Tid(id);
    }

    @JsonIgnore
    public int getTid() {
        return intValue();
    }
}
