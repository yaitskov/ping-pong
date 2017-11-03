package org.dan.ping.pong.sys.db.converters;

import static java.util.Optional.ofNullable;

import org.dan.ping.pong.app.match.Mid;
import org.jooq.Converter;

import java.util.Optional;

public class OptionalMidConverter implements Converter<Integer, Optional<Mid>> {
    public Optional<Mid> from(Integer t) {
        return ofNullable(t).map(Mid::new);
    }

    public Integer to(Optional<Mid> t) {
        return t.map(Mid::getId).orElse(null);
    }

    @Override
    public Class<Integer> fromType() {
        return Integer.class;
    }

    @Override
    public Class<Optional<Mid>> toType() {
        return (Class<Optional<Mid>>) Optional.empty().getClass();
    }
}