package org.dan.ping.pong.app.bid;

import static java.lang.Integer.compare;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.primitives.UnsignedInts;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.dan.ping.pong.sys.hash.HashAggregator;
import org.dan.ping.pong.sys.hash.Hashable;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
public class Uid implements Comparable<Uid>, Hashable {
    public static final String USER_ID_MUST_BE_GREATER_THAN_2 = "user id must be greater than 2";

    @Getter(onMethod = @__(@JsonValue))
    @Min(value = 2, message = USER_ID_MUST_BE_GREATER_THAN_2)
    @Max(value = Short.MAX_VALUE, message = "User is is not big")
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

    @Override
    public void hashTo(HashAggregator sink) {
        sink.hash(id);
    }
}
