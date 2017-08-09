package org.dan.ping.pong.app.bid;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TournamentGroupingBid {
    private int uid;
    private int cid;
    private BidState state;
}
