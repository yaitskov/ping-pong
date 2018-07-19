package org.dan.ping.pong.app.category;

import org.jooq.impl.EnumConverter;

public class CategoryStateConverter extends EnumConverter<String, CategoryState> {
    public CategoryStateConverter() {
        super(String.class, CategoryState.class);
    }
}
