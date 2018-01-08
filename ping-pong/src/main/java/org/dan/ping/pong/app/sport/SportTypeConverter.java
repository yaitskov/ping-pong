package org.dan.ping.pong.app.sport;

import org.jooq.impl.EnumConverter;

public class SportTypeConverter extends EnumConverter<String, SportType> {
    public SportTypeConverter() {
        super(String.class, SportType.class);
    }
}
