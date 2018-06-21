package org.dan.ping.pong.sys.db.converters.num;

import org.jooq.types.UShort;

public abstract class UShortConverter<JN extends Number>
        extends SmallNumConverter<UShort, JN> {

    @Override
    protected UShort toSafe(JN u) {
        return UShort.valueOf(u.shortValue());
    }

    @Override
    protected int maxValue() {
        return UShort.MAX_VALUE;
    }

    @Override
    public Class<UShort> fromType() {
        return UShort.class;
    }
}
