package org.dan.ping.pong.mock.simulator;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.createRndGen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.match.MatchValidationRule;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class GameEnd {
    private final List<Player> participants;
    private final MatchOutcome outcome;
    private final SetGenerator setGenerator;

    public static GameEnd game(Player a, MatchOutcome outcome, Player b,
            MatchValidationRule matchRules) {
        return game(a, outcome, b, createRndGen(a, outcome, b, matchRules));
    }

    public static GameEnd game(Player a, MatchOutcome outcome, Player b,
            SetGenerator setGenerator) {
        return new GameEnd(asList(a, b), outcome, setGenerator);
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
