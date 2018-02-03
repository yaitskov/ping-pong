package org.dan.ping.pong.app.match;

import static java.util.Collections.emptyList;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.tournament.TournamentProgress;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyPendingMatchList {
    private List<MyPendingMatch> matches = emptyList();
    private TournamentProgress progress;
    private boolean showTables;
    private BidState bidState;

    public static class MyPendingMatchListBuilder {
        List<MyPendingMatch> matches = emptyList();
    }
}
