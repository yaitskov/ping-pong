package org.dan.ping.pong.app.bid;

import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

public enum BidState {
    Want, // User is willing to participate in the tournament
    Paid, // once he paid for the ticket
    Here, // passed check right before the tournament
    Play, // participates right now in a match
    Rest, // temporary not available due timeout
    Wait, // wait for place or enemy
    Expl, // disqualified out of tournament due rule breaking, etc...
    Quit, // resigned during or before tournament on a free will
    Lost, // lost tournament battle in a group or playoff game
    Win3 { // won 3rd place
        public int score() {
            return 3;
        }
    },
    Win2 { // won 2nd place
        public int score() {
            return 2;
        }
    },
    Win1 { // won 1st place
        public int score() {
            return 1;
        }
    };
    public static final Set<BidState> TERMINAL_RECOVERABLE_STATES
            = ImmutableSet.of(Win1, Win2, Win3, Lost, Play);

    public static final List<BidState> WIN_STATES = asList(Win1, Win2, Win3);

    public int score() {
        return 4;
    }

    public static final Set<BidState> TERMINAL_STATE = ImmutableSet.of(Expl, Quit);
}
