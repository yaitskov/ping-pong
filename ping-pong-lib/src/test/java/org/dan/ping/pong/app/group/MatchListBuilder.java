package org.dan.ping.pong.app.group;

import static java.util.Collections.singletonList;
import static org.dan.ping.pong.app.match.MatchState.Break;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchTag.DISAMBIGUATION;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchListBuilder {
    private Optional<Integer> ogid;

    final List<MatchInfo> result = new ArrayList<>();
    public static MatchListBuilder matches() {
        return new MatchListBuilder();
    }

    public MatchListBuilder ogid(Integer gid) {
        ogid = Optional.of(gid);
        return this;
    }

    public MatchListBuilder om(Bid u1, Integer g1, Bid u2, Integer g2) {
        result.add(match(u1, g1, u2, g2, Optional.empty()));
        return this;
    }

    public MatchListBuilder brokenMatch(Bid u1, Integer g1, Bid u2, Integer g2) {
        final MatchInfo match = match(u1, g1, u2, g2, Optional.empty());
        match.setState(Break);
        result.add(match);
        return this;
    }

    public MatchListBuilder dm(Bid u1, Integer g1, Bid u2, Integer g2) {
        result.add(match(u1, g1, u2, g2,
                Optional.of(MatchTag.builder().prefix(DISAMBIGUATION).build())));
        return this;
    }

    private MatchInfo match(Bid bid1, int g1, Bid bid2, int g2, Optional<MatchTag> tag) {
        return MatchInfo.builder()
                .gid(ogid)
                .tag(tag)
                .state(Over)
                .participantIdScore(ImmutableMap.of(
                        bid1, singletonList(g1),
                        bid2, singletonList(g2)))
                .winnerId(Optional.of(g1 > g2 ? bid1 : bid2))
                .build();
    }

    public List<MatchInfo> build() {
        return result;
    }
}
