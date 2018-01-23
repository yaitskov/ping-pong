package org.dan.ping.pong.app.tournament.console;

import org.jooq.impl.EnumConverter;

public class TournamentRelationTypeConverter extends EnumConverter<String, TournamentRelationType> {
    public TournamentRelationTypeConverter() {
        super(String.class, TournamentRelationType.class);
    }
}
