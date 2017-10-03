package org.dan.ping.pong.app.server.tournament;

import org.jooq.impl.EnumConverter;

public class TournamentStateConverter extends EnumConverter<String, TournamentState> {
    public TournamentStateConverter() {
        super(String.class, TournamentState.class);
    }
}
