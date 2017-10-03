package org.dan.ping.pong.mock.simulator;

import lombok.Builder;
import lombok.Getter;
import org.dan.ping.pong.app.server.match.OpenMatchForJudge;

import java.util.Set;

@Getter
@Builder
public class MatchMetaInfo {
    private final Set<Player> players;
    private final OpenMatchForJudge openMatch;
}
