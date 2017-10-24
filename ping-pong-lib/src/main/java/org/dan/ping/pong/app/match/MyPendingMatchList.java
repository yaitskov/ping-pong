package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.BidState;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPendingMatchList {
    private List<MyPendingMatch> matches;
    private long totalLeft;
    private boolean showTables;
    private BidState bidState;
}
