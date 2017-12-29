package org.dan.ping.pong.app.tournament;

import org.jooq.impl.EnumConverter;

public class TournamentTypeConverter extends EnumConverter<String, TournamentType> {
    public TournamentTypeConverter() {
        super(String.class, TournamentType.class);
    }
}
