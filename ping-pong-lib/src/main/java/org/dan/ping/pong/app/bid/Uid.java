package org.dan.ping.pong.app.bid;

import static java.lang.Integer.compare;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.Min;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Uid implements Comparable<Uid> {
    public static final String USER_ID_MUST_BE_GREATER_THAN_2 = "user id must be greater than 2";

    @Getter(onMethod = @__(@JsonValue))
    @Min(value = 2, message = USER_ID_MUST_BE_GREATER_THAN_2)
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
