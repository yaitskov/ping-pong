package org.dan.ping.pong.sys.db.converters.num;

import org.dan.ping.pong.app.match.Mid;
import org.jooq.types.UShort;

public class MidConverter extends UShortConverter<Mid> {
    @Override
    protected Mid fromNonNull(UShort u) {
        return new Mid(u.intValue());
    }

    @Override
    public String overflowMessage() {
        return "Match id overflow";
    }

    @Override
    public Class<UShort> fromType() {
        return UShort.class;
    }

    @Override
    public Class<Mid> toType() {
        return Mid.class;
    }
}
