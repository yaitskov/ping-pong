package org.dan.ping.pong.app.bid;

import org.jooq.impl.EnumConverter;

public class BidStateConverter extends EnumConverter<String, BidState> {
    public BidStateConverter() {
        super(String.class, BidState.class);
    }
}
