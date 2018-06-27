package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.tournament.Tid;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchScore {
    private Tid tid;
    private Mid mid;
    private Optional<Bid> winBid;
    private Map<Bid, Integer> wonSets;
    private Map<Bid, List<Integer>> sets;

    public static class MatchScoreBuilder {
        private Optional<Bid> winBid = Optional.empty();
    }
}
