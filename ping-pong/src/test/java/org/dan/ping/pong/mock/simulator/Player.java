package org.dan.ping.pong.mock.simulator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Player implements Comparable<Player> {
    public static final Player pLoser = p(-1);

    public static final Player p1 = p(1);
    public static final Player p2 = p(2);
    public static final Player p3 = p(3);
    public static final Player p4 = p(4);
    public static final Player p5 = p(5);
    public static final Player p6 = p(6);
    public static final Player p7 = p(7);
    public static final Player p8 = p(8);
    public static final Player p9 = p(9);
    public static final Player p10 = p(10);
    public static final Player p11 = p(11);
    public static final Player p12 = p(12);
    public static final Player p13 = p(13);
    public static final Player p14 = p(14);
    public static final Player p15 = p(15);
    public static final Player p16 = p(16);
    public static final Player p17 = p(17);
    public static final Player p18 = p(18);
    public static final Player p19 = p(19);
    public static final Player p20 = p(20);
    public static final Player p21 = p(21);
    public static final Player p22 = p(22);
    public static final Player p23 = p(23);
    public static final Player p24 = p(24);
    public static final Player p25 = p(25);
    public static final Player p26 = p(26);
    public static final Player p27 = p(27);
    public static final Player p28 = p(28);
    public static final Player p29 = p(29);
    public static final Player p30 = p(30);
    public static final Player p31 = p(31);
    public static final Player p32 = p(32);
    public static final Player p33 = p(33);
    public static final Player p34 = p(34);
    public static final Player p35 = p(35);

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
