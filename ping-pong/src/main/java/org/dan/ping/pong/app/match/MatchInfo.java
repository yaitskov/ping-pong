package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchInfo {
    private int mid;
    private int tid;
    private int cid;
    private Optional<Integer> gid;
    private MatchState state;
    private Optional<Integer> loserMid;
    private Optional<Integer> winnerMid;
    private Map<Integer, Integer> participantIdScore;
}
