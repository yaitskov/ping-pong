package org.dan.ping.pong.sys.db.converters.num;

import static java.util.Optional.ofNullable;

import org.jooq.Converter;
import org.jooq.types.UByte;

import java.util.Optional;

public class UByteOptionalConverter
        implements Converter<UByte, Optional<Integer>> {
    @Override
    public Optional<Integer> from(UByte t) {
        return ofNullable(t).map(UByte::intValue);
    }

    @Override
    public UByte to(Optional<Integer> t) {
        return t.map(Integer::shortValue)
                .map(UByte::valueOf)
                .orElse(null);
    }

    @Override
    public Class<UByte> fromType() {
        return UByte.class;
    }

    @Override
    public Class<Optional<Integer>> toType() {
        return (Class<Optional<Integer>>) Optional.empty().getClass();
    }
}
