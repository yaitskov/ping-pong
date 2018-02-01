package org.dan.ping.pong.app.tournament;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Min;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Tid implements Comparable<Tid> {
    public static final String TOURNAMENT_ID_SHOULD_BE_A_POSITIVE_NUMBER = "tournament id should be a positive number";

    @Getter(onMethod = @__(@JsonValue))
    @Min(value = 1, message = TOURNAMENT_ID_SHOULD_BE_A_POSITIVE_NUMBER)
    private final int tid;

    // jax-rsp
    @JsonCreator
    public static Tid valueOf(String s) {
        return new Tid(Integer.valueOf(s));
    }

    public static Tid of(int id) {
        return new Tid(id);
    }

    public String toString() {
        return String.valueOf(tid);
    }

    @Override
    public int compareTo(Tid o) {
        return Integer.compare(tid, o.tid);
    }
}
