package org.dan.ping.pong.mock.simulator;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class ProvidedRank {
    private int value;

    public static ProvidedRank rank(int r) {
        return new ProvidedRank(r);
    }

    public String toString() {
        return "r(" + value + ")";
    }

    public static ProvidedRank R0 = rank(0);
    public static ProvidedRank R1 = rank(1);
    public static ProvidedRank R2 = rank(2);
    public static ProvidedRank R3 = rank(3);
    public static ProvidedRank R4 = rank(4);
    public static ProvidedRank R5 = rank(5);
    public static ProvidedRank R6 = rank(6);
    public static ProvidedRank R11 = rank(11);
}
