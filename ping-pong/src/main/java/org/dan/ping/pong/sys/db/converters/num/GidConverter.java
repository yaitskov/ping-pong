package org.dan.ping.pong.sys.db.converters.num;

import org.dan.ping.pong.app.group.Gid;
import org.jooq.types.UByte;

public class GidConverter extends UByteConverter<Gid> {
    @Override
    protected Gid fromNonNull(UByte u) {
        return new Gid(u.intValue());
    }

    @Override
    public String overflowMessage() {
        return "Group id overflow";
    }

    @Override
    public Class<Gid> toType() {
        return Gid.class;
    }
}
