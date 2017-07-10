package org.dan.ping.pong.mock.simulator;

import static java.util.Arrays.asList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@RequiredArgsConstructor
public class GameEnd implements Scene {
    private final List<Player> participants;
    private final GameOutcome outcome;

    public static GameEnd game(Player a, GameOutcome outcome, Player b) {
        return new GameEnd(asList(a, b), outcome);
    }
}
