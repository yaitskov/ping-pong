package org.dan.ping.pong.app.match.dispute;

import static java.lang.Integer.compare;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class DisputeId implements Comparable<DisputeId> {
    @Getter(onMethod = @__(@JsonValue))
    private final int id;

    // jax-rsp
    @JsonCreator
    public static DisputeId valueOf(String s) {
        return new DisputeId(Integer.valueOf(s));
    }

    public static DisputeId of(int id) {
        return new DisputeId(id);
    }

    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public int compareTo(DisputeId o) {
        return compare(id, o.id);
    }
}
