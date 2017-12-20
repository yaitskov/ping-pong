package org.dan.ping.pong.app.playoff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.match.Mid;

import java.util.Map;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayOffMatch {
    private Mid id;
    private Map<Uid, Integer> score;
    private int level;
    private MatchState state;
}
