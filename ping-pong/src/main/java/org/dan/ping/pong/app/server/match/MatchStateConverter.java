package org.dan.ping.pong.app.server.match;

import org.jooq.impl.EnumConverter;

public class MatchStateConverter extends EnumConverter<String, MatchState> {
    public MatchStateConverter() {
        super(String.class, MatchState.class);
    }
}
