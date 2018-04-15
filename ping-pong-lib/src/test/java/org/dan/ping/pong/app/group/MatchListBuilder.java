package org.dan.ping.pong.app.group;

import static java.util.Collections.singletonList;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Uid;
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

    public MatchListBuilder m(Uid u1, Integer g1, Uid u2, Integer g2) {
        result.add(match(u1, g1, u2, g2, Optional.empty()));
        return this;
    }

    private MatchInfo match(Uid uid1, int g1, Uid uid2, int g2, Optional<MatchTag> tag) {
        return MatchInfo.builder()
                .gid(ogid)
                .tag(tag)
                .participantIdScore(ImmutableMap.of(
                        uid1, singletonList(g1),
                        uid2, singletonList(g2)))
                .winnerId(Optional.of(g1 > g2 ? uid1 : uid2))
                .build();
    }

    public List<MatchInfo> build() {
        return result;
    }
}
