package org.dan.ping.pong.app.playoff;

import static java.util.Arrays.copyOf;

import java.util.HashMap;
import java.util.Map;

public class PowerRange {
    private final Map<Integer, long[]> base2Range = new HashMap<>();

    public long value(int base, int power) {
        return base2Range.compute(base, (b, range) -> {
            if (range == null) {
                return powerOf(base, 1, new long[power + 1]);
            } else if (power >= range.length) {
                return powerOf(base, range.length, copyOf(range, power + 1));
            } else {
                return range;
            }
        })[power];
    }

    public void clear() {
        base2Range.clear();
    }

    private long[] powerOf(long base, int start, long[] result) {
        result[0] = 1;
        for (int i = start; i < result.length; ++i) {
            result[i] = result[i - 1] * base;
        }
        return result;
    }
}
