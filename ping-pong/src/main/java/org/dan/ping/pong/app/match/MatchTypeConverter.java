package org.dan.ping.pong.app.match;

import org.jooq.impl.EnumConverter;

public class MatchTypeConverter extends EnumConverter<String, MatchType> {
    public MatchTypeConverter() {
        super(String.class, MatchType.class);
    }
}
