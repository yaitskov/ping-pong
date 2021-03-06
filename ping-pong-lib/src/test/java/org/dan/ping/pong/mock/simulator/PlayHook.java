package org.dan.ping.pong.mock.simulator;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlayHook {
    private final Hook type;
    private HookCallback callback;

    public HookDecision pauseBefore(TournamentScenario scenario, MatchMetaInfo players) {
        return type.pauseBefore(scenario, players, callback);
    }

    public void pauseAfter(TournamentScenario scenario, MatchMetaInfo players) {
        type.pauseAfter(scenario, players, callback);
    }
}
