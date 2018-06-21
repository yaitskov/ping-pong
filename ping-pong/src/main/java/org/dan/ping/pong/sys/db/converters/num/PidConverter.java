package org.dan.ping.pong.sys.db.converters.num;

import org.dan.ping.pong.app.place.Pid;
import org.jooq.Converter;

public class PidConverter implements Converter<Integer, Pid> {
    @Override
    public Pid from(Integer pid) {
        if (pid == null || pid == 0) {
            return null;
        }
        return new Pid(pid);
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
