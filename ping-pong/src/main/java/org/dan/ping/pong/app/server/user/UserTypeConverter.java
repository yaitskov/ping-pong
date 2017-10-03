package org.dan.ping.pong.app.server.user;

import org.jooq.impl.EnumConverter;

public class UserTypeConverter extends EnumConverter<String, UserType> {
    public UserTypeConverter() {
        super(String.class, UserType.class);
    }
}
