package org.dan.ping.pong.app.server.tournament;

public enum TournamentState implements State {
    Hidden, Announce, Draft, Open, Close, Canceled, Replaced
}
