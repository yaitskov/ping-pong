package org.dan.ping.pong.app.match;

import static java.util.Collections.emptyList;
import static org.dan.ping.pong.app.group.GroupService.MATCH_TAG_DISAMBIGUATION;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.sys.hash.HashAggregator;
import org.dan.ping.pong.sys.hash.Hashable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

@Getter
@EqualsAndHashCode
public class MatchParticipants implements Comparable<MatchParticipants>, Hashable {
    public static final Comparator<MatchParticipants> MATCH_PARTICIPANTS_COMPARATOR = Comparator
            .comparing(MatchParticipants::getBidLess)
            .thenComparing(MatchParticipants::getBidMore);

    private final Bid bidLess;
    private final Bid bidMore;

    public MatchParticipants(Bid bidLess, Bid bidMore) {
        if (bidLess.equals(bidMore)) {
            throw internalError("match uids equal " + bidLess);
        }
        if (bidLess.compareTo(bidMore) < 0) {
            this.bidLess = bidLess;
            this.bidMore = bidMore;
        } else {
            this.bidLess = bidMore;
            this.bidMore = bidLess;
        }
    }

    public static MatchParticipants create(MatchInfo match) {
        if (match.bids().size() != 2) {
            throw internalError("group match doesn't have enough participants");
        }
        final Iterator<Bid> a = match.bids().iterator();
        return new MatchParticipants(a.next(), a.next());
    }

    @Override
    public int compareTo(MatchParticipants o) {
        return MATCH_PARTICIPANTS_COMPARATOR.compare(this, o);
    }

    @Override
    public void hashTo(HashAggregator sink) {
        sink.hash(bidLess).hash(bidMore);
    }

    public MatchInfo toFakeMatch() {
        return MatchInfo.builder().mid(Mid.of(-1))
                .tag(MATCH_TAG_DISAMBIGUATION)
                .participantIdScore(
                        ImmutableMap.of(
                                bidLess, emptyList(),
                                bidMore, emptyList()))
                .build();
    }

    public boolean hasAll(Set<Bid> bids) {
        return bids.contains(bidLess) && bids.contains(bidMore);
    }
}
