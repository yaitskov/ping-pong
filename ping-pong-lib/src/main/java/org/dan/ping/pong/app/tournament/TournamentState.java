package org.dan.ping.pong.app.tournament;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public enum TournamentState implements State {
    Hidden, Announce, Draft, Open, Close, Canceled, Replaced;

    public static final Set<TournamentState> TERMINAL_STATE = ImmutableSet.of(Close, Canceled, Replaced);
}
