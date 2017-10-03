package org.dan.ping.pong.app.tournament;

public enum TournamentState implements State {
    Hidden, Announce, Draft, Open, Close, Canceled, Replaced
}
