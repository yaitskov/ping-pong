package org.dan.ping.pong.app.server.bid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBidState {
    private int tid;
    private int uid;
    private BidState expected;
    private BidState target;
}
