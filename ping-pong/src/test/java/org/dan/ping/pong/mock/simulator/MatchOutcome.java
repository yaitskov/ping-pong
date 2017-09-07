package org.dan.ping.pong.mock.simulator;

import static java.util.Arrays.asList;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MatchOutcome {
    public static final MatchOutcome W30 = new MatchOutcome(3, 0);
    public static final MatchOutcome W31 = new MatchOutcome(3, 1);
    public static final MatchOutcome W32 = new MatchOutcome(3, 2);
    public static final MatchOutcome L03 = W30.flip();
    public static final MatchOutcome L13 = W31.flip();
    public static final MatchOutcome L23 = W32.flip();
    public static final List<MatchOutcome> OUTCOMES = asList(W30, W31, W32, L03, L13, L23);

    private final int first;
    private final int second;

    public int first() {
        return first;
    }

    public int second() {
        return second;
    }

    private MatchOutcome flip() {
        return new MatchOutcome(second, first);
    }
}
