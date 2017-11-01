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
    private Tid tid;
    private String name;
    private BidState outcome;
}
