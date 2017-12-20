package org.dan.ping.pong.app.playoff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayOffMatches {
    private List<PlayOffMatch> matches;
    private List<MatchLink> transitions;
    private Map<Uid, String> participants;
}
