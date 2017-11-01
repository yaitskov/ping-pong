package org.dan.ping.pong.sys.db.converters;

import org.dan.ping.pong.app.place.Pid;
import org.jooq.Converter;

public class PidConverter implements Converter<Integer, Pid> {
    @Override
    public Pid from(Integer uid) {
        if (uid == null || uid == 0) {
            return null;
        }
        return new Pid(uid);
    }

    @Override
    public Integer to(Pid u) {
        if (u == null) {
            return null;
        }
        return u.getPid();
    }

    @Override
    public Class<Integer> fromType() {
        return Integer.class;
    }

    @Override
    public Class<Pid> toType() {
        return Pid.class;
    }
}
