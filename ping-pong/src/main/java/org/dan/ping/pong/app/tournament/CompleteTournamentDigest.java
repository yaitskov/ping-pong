package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dan.ping.pong.app.bid.BidState;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteTournamentDigest {
    private int tid;
    private String name;
    private BidState outcome;
}
