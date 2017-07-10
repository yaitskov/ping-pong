package org.dan.ping.pong.sys.db.converters;

import org.jooq.Converter;

import java.sql.Timestamp;
import java.time.Instant;

public class InstantToTimestampConverter implements Converter<Timestamp, Instant> {
    @Override
    public Instant from(Timestamp databaseObject) {
        if (databaseObject == null) {
            return null;
        }
        return databaseObject.toInstant();
    }

    @Override
    public Timestamp to(Instant userObject) {
        if (userObject == null) {
            return null;
        }
        return Timestamp.from(userObject);
    }

    @Override
    public Class<Timestamp> fromType() {
        return Timestamp.class;
    }

    @Override
    public Class<Instant> toType() {
        return Instant.class;
    }
}
