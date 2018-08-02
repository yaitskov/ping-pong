package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.sys.hash.HashAggregator;
import org.dan.ping.pong.sys.hash.Hashable;

import java.util.Comparator;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantReplace implements Comparable<ParticipantReplace>, Hashable {
    private static final Comparator<ParticipantReplace> COMPARATOR = Comparator
            .comparing(ParticipantReplace::getMid)
            .thenComparing(ParticipantReplace::getLeavingBid)
            .thenComparing(ParticipantReplace::getComingBid);

    private Mid mid;
    private Bid leavingBid;
    private Bid comingBid;

    @Override
    public void hashTo(HashAggregator sink) {
        sink.hash(mid).hash(leavingBid).hash(comingBid);
    }

    @Override
    public int compareTo(ParticipantReplace o) {
        return COMPARATOR.compare(this, o);
    }
}
