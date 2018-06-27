package org.dan.ping.pong.app.playoff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.match.Mid;

import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayOffMatch {
    private Mid id;
    private Map<Bid, Integer> score;
    private int level;
    private MatchState state;
    private boolean walkOver;
    private Optional<Bid> winnerId;
    //private Map<Integer, Uid> groups;
}
