package org.dan.ping.pong.app.playoff;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.tournament.TournamentResultEntry;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class PlayOffBidComparator implements Comparator<PlayOffBidStat> {
    private final Comparator<PlayOffBidStat> base;
    private final List<TournamentResultEntry> groupEntries;

    @Override
    public int compare(PlayOffBidStat o1, PlayOffBidStat o2) {
        final int r = base.compare(o1, o2);
        if (r != 0) {
            return r;
        }
        for (TournamentResultEntry e : groupEntries) {
            final Bid u = e.getUser().getBid();
            if (o1.getBid().equals(u)) {
                return -1;
            } else if (o2.getBid().equals(u)) {
                return 1;
            }
        }
        return 0;
    }
}
