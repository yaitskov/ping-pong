package org.dan.ping.pong.sys.db.converters;

import static java.util.Optional.ofNullable;

import java.util.Optional;

import org.dan.ping.pong.util.Reflector;
import org.jooq.Converter;

public abstract class AbstractOptionalConverter<T> implements Converter<T, Optional<T>> {
    @Override
    public Optional<T> from(T t) {
        return ofNullable(t);
    }

    @Override
    public T to(Optional<T> t) {
        return t.orElseGet(() -> null);
    }

    @Override
    public Class<T> fromType() {
        return (Class<T>) Reflector.genericSuperClass(getClass());
    }

    @Override
    public Class<Optional<T>> toType() {
        return (Class<Optional<T>>) Optional.empty().getClass();
    }
}
