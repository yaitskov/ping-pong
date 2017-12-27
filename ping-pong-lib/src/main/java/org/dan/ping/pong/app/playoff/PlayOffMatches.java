package org.dan.ping.pong.app.playoff;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Uid;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayOffMatches {
    private List<PlayOffMatch> matches;
    private List<MatchLink> transitions = Collections.emptyList();
    private Map<Uid, String> participants;

    public static class PlayOffMatchesBuilder {
        List<MatchLink> transitions = Collections.emptyList();
    }
}
