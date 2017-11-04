package org.dan.ping.pong.app.match.dispute;

import org.dan.ping.pong.app.bid.BidState;
import org.jooq.impl.EnumConverter;

public class DisputeStateConverter extends EnumConverter<String, DisputeStatus> {
    public DisputeStateConverter() {
        super(String.class, DisputeStatus.class);
    }
}
