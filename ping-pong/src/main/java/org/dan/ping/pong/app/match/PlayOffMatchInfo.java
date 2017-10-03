package org.dan.ping.pong.app.match;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PlayOffMatchInfo {
    private int mid;
    private int drafted;
    private int cid;
    private MatchState state;
    private int tid;

    public void incrementDrafted() {
        ++drafted;
    }
}
