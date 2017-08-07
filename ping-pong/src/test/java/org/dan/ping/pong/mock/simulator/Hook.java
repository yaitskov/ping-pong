package org.dan.ping.pong.mock.simulator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum Hook {
    BeforeScore {
        @Override
        public HookDecision pauseBefore(TournamentScenario scenario,
                MatchMetaInfo players, HookCallback callback) {
            return callback.apply(scenario, players);
        }

        @Override
        public void pauseAfter(TournamentScenario scenario,
                MatchMetaInfo players, HookCallback callback) {
        }
    },
    AfterScore {
        @Override
        public HookDecision pauseBefore(TournamentScenario scenario,
                MatchMetaInfo players, HookCallback callback) {
            return HookDecision.Score;
        }

        @Override
        public void pauseAfter(TournamentScenario scenario,
                MatchMetaInfo players, HookCallback callback) {
            callback.apply(scenario, players);
        }
    },
    NonStop {
        @Override
        public HookDecision pauseBefore(TournamentScenario scenario,
                MatchMetaInfo players, HookCallback callback) {
            return HookDecision.Score;
        }

        @Override
        public void pauseAfter(TournamentScenario scenario,
                MatchMetaInfo players, HookCallback callback) {
        }
    };

    @SneakyThrows
    public static HookDecision pause(Hook when, MatchMetaInfo players) {
        log.info("Pause " + when
                        + " scoring match between players {}\n"
                        + "Press Enter to continue",
                players.getPlayers());
        System.in.read();
        return HookDecision.Score;
    }

    public abstract HookDecision pauseBefore(TournamentScenario scenario, MatchMetaInfo players, HookCallback callback);
    public abstract void pauseAfter(TournamentScenario scenario, MatchMetaInfo players, HookCallback callback);
}
