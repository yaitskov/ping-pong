package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.match.MatchType.Brnz;
import static org.dan.ping.pong.app.match.MatchType.Gold;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Builder
public class PlayOffGenerator {
    private final OpenTournamentMemState tournament;
    private final int cid;
    private final boolean thirdPlaceMatch;
    private final MatchDao matchDao;

    public Optional<Integer> generateTree(int level, Optional<Integer> parentMid,
            int priority, TypeChain types, Optional<Integer> loserMid) {
        if (level < MatchDao.FIRST_PLAY_OFF_MATCH_LEVEL) {
            return Optional.empty();
        }
        final int mid = matchDao.createPlayOffMatch(
                tournament.getTid(), cid, parentMid, loserMid, priority, level, types.getType());
        final Optional<Integer> omid = Optional.of(mid);
        tournament.getMatches().put(mid, MatchInfo.builder()
                .tid(tournament.getTid())
                .mid(mid)
                .state(MatchState.Draft)
                .gid(Optional.empty())
                .participantIdScore(new HashMap<>())
                .type(types.getType())
                .winnerMid(parentMid)
                .loserMid(loserMid)
                .cid(cid)
                .build());
        log.info("Play off match {}:{} of tournament {} in category {}",
                types.getType(), omid, tournament.getTid(), cid);
        Optional<Integer> midBronze = Optional.empty();
        if (thirdPlaceMatch && types.getType() == Gold) {
            midBronze = Optional.of(matchDao.createPlayOffMatch(
                    tournament.getTid(), cid, parentMid, Optional.empty(), priority, level, Brnz));
        }
        generateTree(level - 1, omid, priority - 1, types.next(), midBronze);
        generateTree(level - 1, omid, priority - 1, types.next(), midBronze);
        return omid;
    }
}
