package org.dan.ping.pong.sys.db.converters.num;

import org.dan.ping.pong.app.tournament.Tid;
import org.jooq.Converter;

public class TidConverter implements Converter<Integer, Tid> {
    @Override
    public Tid from(Integer uid) {
        if (uid == null || uid == 0) {
            return null;
        }
        return new Tid(uid);
    }

    @Override
    public Integer to(Tid u) {
        if (u == null) {
            return null;
        }
        return u.getTid();
    }

    @Override
    public Class<Integer> fromType() {
        return Integer.class;
    }

    @Override
    public Class<Tid> toType() {
        return Tid.class;
    }
}
