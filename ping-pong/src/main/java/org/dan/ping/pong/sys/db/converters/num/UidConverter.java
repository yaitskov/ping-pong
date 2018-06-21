package org.dan.ping.pong.sys.db.converters.num;

import org.dan.ping.pong.app.bid.Uid;
import org.jooq.Converter;

public class UidConverter implements Converter<Integer, Uid> {
    @Override
    public Uid from(Integer uid) {
        if (uid == null || uid == 0) {
            return null;
        }
        return new Uid(uid);
    }

    @Override
    public Integer to(Uid u) {
        if (u == null) {
            return null;
        }
        return u.getId();
    }

    @Override
    public Class<Integer> fromType() {
        return Integer.class;
    }

    @Override
    public Class<Uid> toType() {
        return Uid.class;
    }
}
