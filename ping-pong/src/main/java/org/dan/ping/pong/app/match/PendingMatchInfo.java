package org.dan.ping.pong.app.match;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@Getter
@Builder
@ToString
public class PendingMatchInfo {
    private int mid;
    private List<Integer> uids;
}
