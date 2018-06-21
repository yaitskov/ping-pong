package org.dan.ping.pong.sys.db.converters.num;

import static java.util.Optional.ofNullable;

import org.dan.ping.pong.app.match.Mid;
import org.jooq.Converter;
import org.jooq.types.UShort;

import java.util.Optional;

public class OptionalMidConverter implements Converter<UShort, Optional<Mid>> {
    public Optional<Mid> from(UShort t) {
        return ofNullable(t).map(UShort::intValue).map(Mid::new);
    }

    public UShort to(Optional<Mid> t) {
        return t.map(Mid::shortValue)
                .map(UShort::valueOf)
                .orElse(null);
    }

    @Override
    public Class<UShort> fromType() {
        return UShort.class;
    }

    @Override
    public Class<Optional<Mid>> toType() {
        return (Class<Optional<Mid>>) Optional.empty().getClass();
    }
}