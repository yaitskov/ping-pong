package org.dan.ping.pong.util.counter;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.dan.ping.pong.app.match.dispute.DisputeId;
import org.dan.ping.pong.sys.type.number.MutableNumber;

import java.util.Optional;

public class DidSeqGen extends MutableNumber {
    @JsonCreator
    public DidSeqGen(int v) {
        super(v);
    }

    public DidSeqGen(DisputeId v) {
        super(v.intValue());
    }

    public DisputeId next() {
        return DisputeId.of(iGet());
    }

    public static DidSeqGen of(Optional<DisputeId> max) {
        return max.map(m -> new DidSeqGen(m.intValue()))
                .orElseGet(() -> new DidSeqGen(0));
    }
}
