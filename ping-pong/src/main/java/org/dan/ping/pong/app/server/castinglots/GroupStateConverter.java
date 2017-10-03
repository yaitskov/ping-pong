package org.dan.ping.pong.app.server.castinglots;

import org.jooq.impl.EnumConverter;

public class GroupStateConverter extends EnumConverter<String, GroupState> {
    public GroupStateConverter() {
        super(String.class, GroupState.class);
    }
}
