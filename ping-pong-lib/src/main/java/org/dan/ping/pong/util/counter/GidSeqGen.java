package org.dan.ping.pong.util.counter;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.sys.type.number.MutableNumber;

import java.util.Optional;

public class GidSeqGen extends MutableNumber {
    @JsonCreator
    public GidSeqGen(int v) {
        super(v);
    }

    public GidSeqGen(Gid v) {
        super(v.intValue());
    }

    public Gid next() {
        return Gid.of(iGet());
    }

    public static GidSeqGen of(Optional<Gid> max) {
        return max.map(m -> new GidSeqGen(m.intValue()))
                .orElseGet(() -> new GidSeqGen(0));
    }
}
