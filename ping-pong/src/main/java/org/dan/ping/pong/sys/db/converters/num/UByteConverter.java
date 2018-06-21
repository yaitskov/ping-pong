package org.dan.ping.pong.sys.db.converters.num;

import org.jooq.types.UByte;

public abstract class UByteConverter<JN extends Number>
        extends SmallNumConverter<UByte, JN> {

    @Override
    protected UByte toSafe(JN u) {
        return UByte.valueOf(u.byteValue());
    }

    @Override
    protected int maxValue() {
        return UByte.MAX_VALUE;
    }

    @Override
    public Class<UByte> fromType() {
        return UByte.class;
    }
}
