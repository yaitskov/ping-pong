package org.dan.ping.pong.mock.simulator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Player implements Comparable<Player> {
    public static final Player p1 = p(1);
    public static final Player p2 = p(2);
    public static final Player p3 = p(3);
    public static final Player p4 = p(4);
    public static final Player p5 = p(5);
    public static final Player p6 = p(6);
    public static final Player p7 = p(7);
    public static final Player p8 = p(8);
    public static final Player p9 = p(9);
    public static final Player pa = p(10);
    public static final Player pb = p(11);
    public static final Player pc = p(12);

    @Getter
    private final int number;

    public static Player p(int n) {
        return new Player(n);
    }

    @Override
    public String toString() {
        return String.format("p(%d)", number);
    }

    @Override
    public int compareTo(Player o) {
        return Integer.compare(number, o.number);
    }
}
