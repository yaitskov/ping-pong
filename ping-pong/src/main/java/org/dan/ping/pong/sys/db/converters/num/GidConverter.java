package org.dan.ping.pong.sys.db.converters.num;

import org.jooq.types.UByte;

public class GidConverter extends UByteConverter<Integer> {
    @Override
    protected Integer fromNonNull(UByte u) {
        return u.intValue();
    }

    @Override
    public String overflowMessage() {
        return "Group id overflow";
    }

    @Override
    public Class<Integer> toType() {
        return Integer.class;
    }
}
