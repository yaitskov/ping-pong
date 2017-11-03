package org.dan.ping.pong.app.score;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Uid;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class ScoreInfo {
    private Uid uid;
    private int tid;
    private int score;
    private int won;
}
