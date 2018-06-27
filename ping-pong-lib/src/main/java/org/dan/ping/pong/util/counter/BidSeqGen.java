package org.dan.ping.pong.util.counter;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.sys.type.number.MutableNumber;

import java.util.Optional;

public class BidSeqGen extends MutableNumber {
    @JsonCreator
    public BidSeqGen(int v) {
        super(v);
    }

    public BidSeqGen(Bid v) {
        super(v.intValue());
    }

    public Bid next() {
        return Bid.of(iGet());
    }

    public static BidSeqGen of(Optional<Bid> max) {
        return max.map(m -> new BidSeqGen(m.intValue()))
                .orElseGet(() -> new BidSeqGen(0));
    }
}
