package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.hash.HashAggregator;
import org.dan.ping.pong.sys.hash.Hashable;

import java.util.Comparator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TidMid implements Comparable<TidMid>, Hashable {
    private static final Comparator<TidMid> COMPARATOR = Comparator
            .comparing(TidMid::getTid).thenComparing(TidMid::getMid);
    private Tid tid;
    private Mid mid;

    public static TidMid tidMidOf(Tid tid, Mid mid) {
        return new TidMid(tid, mid);
    }

    @Override
    public void hashTo(HashAggregator sink) {
        sink.hash(tid).hash(mid);
    }

    @Override
    public int compareTo(TidMid o) {
        return COMPARATOR.compare(this, o);
    }
}
