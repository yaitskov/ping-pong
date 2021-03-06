package org.dan.ping.pong.mock.simulator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class PlayerCategory implements Comparable<PlayerCategory> {
    public static final PlayerCategory c1 = category(1);
    public static final PlayerCategory c2 = category(2);
    public static final PlayerCategory c3 = category(3);
    public static final PlayerCategory c4 = category(4);

    private final int number;

    public static PlayerCategory category(int n) {
        return new PlayerCategory(n);
    }

    @Override
    public String toString() {
        return String.format("c(%d)", number);
    }

    @Override
    public int compareTo(PlayerCategory o) {
        return Integer.compare(number, o.number);
    }
}
