package org.dan.ping.pong.app.castinglots.rank;

import lombok.Builder;
import lombok.Getter;
import org.dan.ping.pong.app.bid.Uid;

import java.util.Comparator;

@Getter
@Builder
public class UidIdx {
    private final Uid uid;
    private final int index;

    public static Comparator<UidIdx> uidIdxComparator = Comparator
            .comparing(UidIdx::getIndex)
            .thenComparing(UidIdx::getUid);
}
