package org.dan.ping.pong.app.score;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class ScoreInfo {
    private int uid;
    private int tid;
    private int score;
    private int won;
}
