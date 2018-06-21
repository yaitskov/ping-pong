package org.dan.ping.pong.sys.db.converters.num;

import org.dan.ping.pong.app.match.dispute.DisputeId;
import org.jooq.types.UByte;

public class DidConverter extends UByteConverter<DisputeId> {
    @Override
    protected DisputeId fromNonNull(UByte u) {
        return new DisputeId(u.intValue());
    }

    @Override
    public String overflowMessage() {
        return "Dispute id overflow";
    }

    @Override
    public Class<DisputeId> toType() {
        return DisputeId.class;
    }
}
