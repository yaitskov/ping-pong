package org.dan.ping.pong.sys.db.converters;

import org.dan.ping.pong.app.match.dispute.DisputeId;
import org.jooq.Converter;

public class DidConverter implements Converter<Integer, DisputeId> {
    @Override
    public DisputeId from(Integer pid) {
        if (pid == null || pid == 0) {
            return null;
        }
        return new DisputeId(pid);
    }

    @Override
    public Integer to(DisputeId u) {
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
    public Class<DisputeId> toType() {
        return DisputeId.class;
    }
}
