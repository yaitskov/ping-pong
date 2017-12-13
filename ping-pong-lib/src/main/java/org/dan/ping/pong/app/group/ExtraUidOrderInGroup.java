package org.dan.ping.pong.app.group;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import lombok.Builder;
import lombok.Getter;
import org.dan.ping.pong.app.bid.Uid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Builder
public class ExtraUidOrderInGroup {
    private SetMultimap<Uid, Uid> strongerOf;
    private Set<Uid> diced;
    private Map<Uid, BidSuccessInGroup> uid2SetsAndBalls;

    public static ExtraUidOrderInGroup create() {
        return ExtraUidOrderInGroup.builder()
                .strongerOf(HashMultimap.create())
                .diced(new HashSet<>())
                .uid2SetsAndBalls(new HashMap<>())
                .build();
    }
}
