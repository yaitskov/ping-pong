package org.dan.ping.pong.app.castinglots.rank;

import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProvidedRankOptions {
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String V = "v";

    private String label;
    private int maxValue;
    private int minValue;

    public void validate(int rank) {
        if (rank < minValue || rank > maxValue) {
            throw badRequest("rank out of range",
                    ImmutableMap.of(V, rank, MIN, minValue, MAX, maxValue));
        }
    }
}
