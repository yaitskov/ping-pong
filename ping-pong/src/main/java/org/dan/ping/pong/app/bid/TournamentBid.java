package org.dan.ping.pong.app.bid;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TournamentBid {
    private int uid;
    private int cid;
}
