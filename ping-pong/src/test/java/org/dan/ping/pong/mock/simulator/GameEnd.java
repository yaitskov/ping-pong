package org.dan.ping.pong.mock.simulator;

import static java.util.Arrays.asList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class GameEnd implements Scene {
    private final List<Player> participants;
    private final GameOutcome outcome;

    public static GameEnd game(Player a, GameOutcome outcome, Player b) {
        return new GameEnd(asList(a, b), outcome);
    }

    public String toString() {
        if (outcome.first() < outcome.second()) {
            return participants.get(0) + " L"
                    + outcome.first() + ""
                    + outcome.second() + " " + participants.get(1);
        }
        return participants.get(0) + " W"
                + outcome.first() + ""
                + outcome.second()
                + " " + participants.get(1);
    }
}
