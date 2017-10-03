package org.dan.ping.pong.app.server.match;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class PendingMatchInfo {
    private int mid;
    private List<Integer> uids;
}
