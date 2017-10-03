package org.dan.ping.pong.app.playoff;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PlayOffService {
    public List<MatchInfo> findBaseMatches(List<MatchInfo> cidMatches) {
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

    public List<MatchInfo> findPlayOffMatches(OpenTournamentMemState tournament, int cid) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getCid() == cid)
                .filter(minfo -> !minfo.getGid().isPresent())
                .sorted(Comparator.comparingInt(MatchInfo::getMid))
                .collect(toList());
    }
}
