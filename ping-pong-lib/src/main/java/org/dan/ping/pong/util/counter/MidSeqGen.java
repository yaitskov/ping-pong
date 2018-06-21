package org.dan.ping.pong.util.counter;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.sys.type.number.MutableNumber;

import java.util.Optional;

public class MidSeqGen extends MutableNumber {
    @JsonCreator
    public MidSeqGen(int v) {
        super(v);
    }

    public MidSeqGen(Mid v) {
        super(v.intValue());
    }

    public Mid next() {
        return Mid.of(iGet());
    }

    public static MidSeqGen of(Optional<Mid> max) {
        return max.map(m -> new MidSeqGen(m.intValue()))
                .orElseGet(() -> new MidSeqGen(0));
    }
}
