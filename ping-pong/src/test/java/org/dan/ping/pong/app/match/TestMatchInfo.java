package org.dan.ping.pong.app.match;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class TestMatchInfo {
    private int mid;
    private MatchState state;
    private List<ParticipantScoreInfo> scores;
}
