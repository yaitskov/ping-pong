package org.dan.ping.pong.app.match;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@Getter
@Builder
@ToString
public class OneOpponentMatch {
    private int mid;
    private int uid;
    private Optional<Integer> winnerMid;
}
