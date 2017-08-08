package org.dan.ping.pong.mock.simulator;

import static java.util.Arrays.asList;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GameOutcome {
    public static final GameOutcome W30 = new GameOutcome(3, 0);
    public static final GameOutcome W31 = new GameOutcome(3, 1);
    public static final GameOutcome W32 = new GameOutcome(3, 2);
    public static final GameOutcome L03 = W30.flip();
    public static final GameOutcome L13 = W31.flip();
    public static final GameOutcome L23 = W32.flip();
    public static final List<GameOutcome> OUTCOMES = asList(W30, W31, W32, L03, L13, L23);

    public static final GameOutcome Win = W31;
    public static final GameOutcome Lose = Win.flip();


    private final int first;
    private final int second;

    public int first() {
        return first;
    }

    public int second() {
            return second;
    }

    private GameOutcome flip() {
        return new GameOutcome(second, first);
    }
}
