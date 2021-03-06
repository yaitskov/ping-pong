package org.dan.ping.pong.app.playoff;

import static java.util.Collections.emptyList;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Bid;

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
    private List<RootTaggedMatch> rootTaggedMatches = emptyList();
    private List<MatchLink> transitions = emptyList();
    private Map<Bid, String> participants;

    public static class PlayOffMatchesBuilder {
        List<MatchLink> transitions = emptyList();
        List<RootTaggedMatch> rootTaggedMatches = emptyList();
    }
}
