package org.dan.ping.pong.mock.simulator;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class PlayHook {
    private final Hook type;
    private HookCallback callback;

    public HookDecision pauseBefore(TournamentScenario scenario, Set<Player> players) {
        return type.pauseBefore(scenario, players, callback);
    }

    public void pauseAfter(TournamentScenario scenario, Set<Player> players) {
        type.pauseAfter(scenario, players, callback);
    }
}
