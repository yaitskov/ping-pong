package org.dan.ping.pong.app.bid.result;

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
public class BidMatchesStat {
    private int won;
    private int lost;
    private int total;
}
