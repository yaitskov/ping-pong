package org.dan.ping.pong.util.counter;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.dan.ping.pong.sys.type.number.MutableNumber;

import java.util.Optional;

public class IdSeqGen extends MutableNumber {
    @JsonCreator
    public IdSeqGen(int v) {
        super(v);
    }

    public int next() {
        return iGet();
    }

    public static IdSeqGen of(Optional<Integer> v) {
        return v.map(IdSeqGen::new)
                .orElseGet(() -> new IdSeqGen(0));
    }
}
