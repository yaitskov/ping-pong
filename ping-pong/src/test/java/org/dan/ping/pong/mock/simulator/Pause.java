package org.dan.ping.pong.mock.simulator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public enum Pause {
    BeforeScore {
        @Override
        public void pauseBefore(Set<Player> players) {
            pause("before", players);
        }

        @Override
        public void pauseAfter(Set<Player> players) {
        }
    },
    AfterScore {
        @Override
        public void pauseBefore(Set<Player> players) {
        }

        @Override
        public void pauseAfter(Set<Player> players) {
            pause("after", players);
        }
    },
    NonStop {
        @Override
        public void pauseBefore(Set<Player> players) {
        }

        @Override
        public void pauseAfter(Set<Player> players) {
        }
    };

    @SneakyThrows
    protected void pause(String when, Set<Player> players) {
        log.info("Pause " + when + " scoring match between players {}\nPress Enter to continue",
                players);
        System.in.read();
    }

    public abstract void pauseBefore(Set<Player> players);
    public abstract void pauseAfter(Set<Player> players);
}
