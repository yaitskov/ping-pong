package org.dan.ping.pong.app.match.rule;

import static java.util.stream.Collectors.toMap;

import lombok.AllArgsConstructor;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

@AllArgsConstructor
public class UidsProvider {
    private Set<Uid> uids;
    private Supplier<Stream<MatchInfo>> matchesSupplier;

    public Set<Uid> uids() {
        return uids = findUids(uids, matchesSupplier);
    }

    public int size() {
        return uids().size();
    }

    public static boolean isAllUidsAndNotKnown(Set<Uid> uids) {
        return uids.isEmpty();
    }

    public static Set<Uid> findUids(Set<Uid> uids,
            Supplier<Stream<MatchInfo>> matches) {
        if (isAllUidsAndNotKnown(uids)) {
            return matches.get().flatMap(m -> m.getUids().stream())
                    .collect(toMap(o -> o, o -> o, (a, b) -> a))
                    .keySet();
        }
        return uids;
    }

}
