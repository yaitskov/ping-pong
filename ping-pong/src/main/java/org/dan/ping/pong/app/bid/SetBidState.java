package org.dan.ping.pong.app.bid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.Uid;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBidState {
    private int tid;
    private Uid uid;
    private BidState expected;
    private BidState target;
}
