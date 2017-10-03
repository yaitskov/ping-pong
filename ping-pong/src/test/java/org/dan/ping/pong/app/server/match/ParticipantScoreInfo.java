package org.dan.ping.pong.app.server.match;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ParticipantScoreInfo {
    private int uid;
    private int won;
    private int score;
}
