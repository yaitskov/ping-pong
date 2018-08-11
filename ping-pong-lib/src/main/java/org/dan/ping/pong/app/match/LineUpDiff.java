package org.dan.ping.pong.app.match;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@Setter
@Builder
public class LineUpDiff {
    private Set<Bid> toBeEnlisted;
    private List<Bid> toBeUnlistedOrMoved;

    public List<Bid> toBeUnlisted() {
        return toBeUnlistedOrMoved.stream()
                .filter(bid -> !toBeEnlisted.contains(bid))
                .collect(toList());
    }

    public Set<Bid> toBeMoved() {
        return Stream
                .concat(toBeEnlisted.stream(),
                        toBeUnlistedOrMoved.stream())
                .collect(toSet()) ;
    }
}
