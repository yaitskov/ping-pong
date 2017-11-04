package org.dan.ping.pong.app.category;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.List;
import java.util.Set;

@Slf4j
public class CategoryService {
    public Set<Integer> findIncompleteCategories(TournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getState() != MatchState.Over)
                .map(MatchInfo::getCid)
                .collect(toSet());
    }

    public List<MatchInfo> findMatchesInCategory(TournamentMemState tournament, int cid) {
        return tournament.getMatches().values().stream().filter(m -> m.getCid() == cid)
                .collect(toList());
    }
}
