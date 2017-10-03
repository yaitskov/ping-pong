package org.dan.ping.pong.app.table;

import org.jooq.impl.EnumConverter;

public class TableStateConverter extends EnumConverter<String, TableState> {
    public TableStateConverter() {
        super(String.class, TableState.class);
    }
}
