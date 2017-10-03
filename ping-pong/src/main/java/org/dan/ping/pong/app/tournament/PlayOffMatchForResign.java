package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.match.MatchType;

import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
public class PlayOffMatchForResign {
    private int mid;
    private MatchState state;
    private MatchType type;
    private Optional<Integer> winMatch;
    private Optional<Integer> lostMatch;
    private Optional<Integer> opponentId;
}
