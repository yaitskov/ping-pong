package org.dan.ping.pong.mock.simulator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public enum Hook {
    BeforeScore {
        @Override
        public HookDecision pauseBefore(TournamentScenario scenario, Set<Player> players, HookCallback callback) {
            return callback.apply(scenario, players);
        }

        @Override
        public void pauseAfter(TournamentScenario scenario, Set<Player> players, HookCallback callback) {
        }
    },
    AfterScore {
        @Override
        public HookDecision pauseBefore(TournamentScenario scenario, Set<Player> players, HookCallback callback) {
            return HookDecision.Score;
        }

        @Override
        public void pauseAfter(TournamentScenario scenario, Set<Player> players, HookCallback callback) {
            callback.apply(scenario, players);
        }
    },
    NonStop {
        @Override
        public HookDecision pauseBefore(TournamentScenario scenario, Set<Player> players, HookCallback callback) {
            return HookDecision.Score;
        }

        @Override
        public void pauseAfter(TournamentScenario scenario, Set<Player> players, HookCallback callback) {
        }
    };

    @SneakyThrows
    public static HookDecision pause(Hook when, Set<Player> players) {
        log.info("Pause " + when + " scoring match between players {}\nPress Enter to continue",
                players);
        System.in.read();
        return HookDecision.Score;
    }

    public abstract HookDecision pauseBefore(TournamentScenario scenario, Set<Player> players, HookCallback callback);
    public abstract void pauseAfter(TournamentScenario scenario, Set<Player> players, HookCallback callback);
}
