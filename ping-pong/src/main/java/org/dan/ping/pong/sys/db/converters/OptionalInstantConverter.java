package org.dan.ping.pong.sys.db.converters;

import org.jooq.Converter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class OptionalInstantConverter implements Converter<Timestamp, Optional<Instant>> {
    @Override
    public Optional<Instant> from(Timestamp s) {
        return Optional.ofNullable(s)
                .map(Timestamp::toInstant);
    }

    @Override
    public Timestamp to(Optional<Instant> s) {
        return s.map(Timestamp::from)
                .orElse(null);
    }

    @Override
    public Class<Timestamp> fromType() {
        return Timestamp.class;
    }

    @Override
    public Class<Optional<Instant>> toType() {
        return (Class<Optional<Instant>>)Optional.empty().getClass();
    }
}
