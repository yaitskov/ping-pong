package org.dan.ping.pong.util.counter;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.sys.type.number.MutableNumber;

import java.util.Optional;

public class CidSeqGen extends MutableNumber {
    @JsonCreator
    public CidSeqGen(int v) {
        super(v);
    }

    public CidSeqGen(Cid v) {
        super(v.intValue());
    }

    public Cid next() {
        return Cid.of(iGet());
    }

    public static CidSeqGen of(Optional<Cid> max) {
        return max.map(m -> new CidSeqGen(m.intValue()))
                .orElseGet(() -> new CidSeqGen(0));
    }
}
