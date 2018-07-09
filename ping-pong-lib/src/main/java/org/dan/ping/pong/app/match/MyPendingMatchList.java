package org.dan.ping.pong.app.match;

import static java.util.Collections.emptyList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.tournament.TournamentProgress;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyPendingMatchList {
    private List<MyPendingMatch> matches = emptyList();
    private TournamentProgress progress;
    private boolean showTables;
    private Map<Integer, BidState> bidState;

    public static class MyPendingMatchListBuilder {
        List<MyPendingMatch> matches = emptyList();
    }

    public MyPendingMatchList merge(MyPendingMatchList b) {
        return MyPendingMatchList
                .builder()
                .progress(progress.merge(b.progress))
                .showTables(showTables || b.showTables)
                .matches(ImmutableList
                        .<MyPendingMatch>builder()
                        .addAll(matches)
                        .addAll(b.matches)
                        .build())
                .bidState(ImmutableMap
                        .<Integer, BidState>builder()
                        .putAll(bidState)
                        .putAll(b.bidState)
                        .build())
                .build();
    }
}
