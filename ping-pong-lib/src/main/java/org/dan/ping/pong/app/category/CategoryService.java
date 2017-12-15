package org.dan.ping.pong.app.category;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class CategoryService {
    public Set<Integer> findIncompleteCategories(TournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getState() != MatchState.Over)
                .map(MatchInfo::getCid)
                .collect(toSet());
    }

    public Stream<MatchInfo> findMatchesInCategoryStream(TournamentMemState tournament, int cid) {
        return tournament.getMatches().values().stream().filter(m -> m.getCid() == cid);
    }

    public List<MatchInfo> findMatchesInCategory(TournamentMemState tournament, int cid) {
        return findMatchesInCategoryStream(tournament, cid).collect(toList());
    }

    public CategoryInfo categoryInfo(TournamentMemState tournament,
            int cid, Optional<Uid> ouid) {
        tournament.checkCategory(cid);
        final CategoryLink cLink = tournament.getCategory(cid);
        return CategoryInfo.builder()
                .link(cLink)
                .role(tournament.
                        detectRole(ouid))
                .users(tournament.getParticipants().values().stream()
                        .filter(m -> m.getCid() == cid)
                        .map(ParticipantMemState::toLink)
                        .collect(toList()))
                .build();
    }
}
