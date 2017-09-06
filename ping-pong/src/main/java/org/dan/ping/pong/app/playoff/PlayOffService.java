package org.dan.ping.pong.app.playoff;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import org.dan.ping.pong.app.match.MatchInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PlayOffService {
    public Collection<MatchInfo> findBaseMatches(List<MatchInfo> cidMatches) {
        Map<Integer, Integer> midChild = new HashMap<>();
        cidMatches.forEach(m -> {
            m.getWinnerMid().ifPresent(wmid -> midChild.put(wmid, m.getMid()));
            m.getLoserMid().ifPresent(wmid -> midChild.put(wmid, m.getMid()));
        });
        return cidMatches.stream()
                .filter(m -> !m.getGid().isPresent())
                .filter(m -> !midChild.containsKey(m.getMid()))
                .collect(toList());
    }

    public Collection<MatchInfo> findNextMatches(Map<Integer, MatchInfo> matches,
            Collection<MatchInfo> baseMatches) {
        return baseMatches.stream()
                .flatMap(m -> Stream.of(
                        ofNullable(matches.get(m.getWinnerMid().orElse(0))),
                        ofNullable(matches.get(m.getLoserMid().orElse(0))))
                        .filter(Optional::isPresent)
                        .map(Optional::get))
                .collect(toMap(MatchInfo::getMid, o -> o, (a, b) -> a))
                .values();
    }

    public Collection<MatchInfo> findGroupMatches(List<MatchInfo> matches) {
        return matches.stream().filter(m -> m.getGid().isPresent()).collect(toList());
    }
}
