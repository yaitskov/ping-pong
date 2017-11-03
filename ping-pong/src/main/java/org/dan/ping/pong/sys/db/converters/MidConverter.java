package org.dan.ping.pong.sys.db.converters;

import org.dan.ping.pong.app.match.Mid;
import org.jooq.Converter;

public class MidConverter implements Converter<Integer, Mid> {
    @Override
    public Mid from(Integer Mid) {
        if (Mid == null || Mid == 0) {
            return null;
        }
        return new Mid(Mid);
    }

    @Override
    public Integer to(Mid u) {
        if (u == null) {
            return null;
        }
        return u.getId();
    }

    @Override
    public Class<Integer> fromType() {
        return Integer.class;
    }

    @Override
    public Class<Mid> toType() {
        return Mid.class;
    }
}
