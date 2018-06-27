package org.dan.ping.pong.app.castinglots.rank;

import lombok.Builder;
import lombok.Getter;
import org.dan.ping.pong.app.bid.Bid;

import java.util.Comparator;

@Getter
@Builder
public class BidIdx {
    private final Bid bid;
    private final int index;

    public static Comparator<BidIdx> uidIdxComparator = Comparator
            .comparing(BidIdx::getIndex)
            .thenComparing(BidIdx::getBid);
}
