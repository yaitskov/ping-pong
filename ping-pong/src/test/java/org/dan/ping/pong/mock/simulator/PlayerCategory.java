package org.dan.ping.pong.mock.simulator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class PlayerCategory {
    public static final PlayerCategory c1 = category(1);

    private final int number;

    public static PlayerCategory category(int n) {
        return new PlayerCategory(n);
    }

    @Override
    public String toString() {
        return String.format("c(%d)", number);
    }
}
