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
    public static final Player ph = p(17);
    public static final Player pi = p(18);
    public static final Player pj = p(19);
    public static final Player pk = p(20);
    public static final Player pl = p(21);
    public static final Player pm = p(22);
    public static final Player pn = p(23);
    public static final Player po = p(24);
    public static final Player pp = p(25);
    public static final Player pq = p(26);
    public static final Player pr = p(27);
    public static final Player ps = p(28);
    public static final Player pt = p(29);
    public static final Player pu = p(30);
    public static final Player pv = p(31);
    public static final Player pw = p(32);
    public static final Player px = p(33);
    public static final Player py = p(34);
    public static final Player pz = p(35);

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
