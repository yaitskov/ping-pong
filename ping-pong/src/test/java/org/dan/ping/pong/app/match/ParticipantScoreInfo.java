package org.dan.ping.pong.app.match;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.dan.ping.pong.app.tournament.Uid;

@Getter
@Builder
@ToString
public class ParticipantScoreInfo {
    private Uid uid;
    private int won;
    private int score;
}
