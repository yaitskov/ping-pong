package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.match.MatchType.Brnz;
import static org.dan.ping.pong.app.match.MatchType.Gold;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.match.MatchDao;

import java.util.Optional;

@Slf4j
@Builder
public class PlayOffGenerator {
    private final int tid;
    private final int cid;
    private final boolean thirdPlaceMatch;
    private final MatchDao matchDao;

    public Optional<Integer> generateTree(int level, Optional<Integer> parentMid,
            int priority, TypeChain types, Optional<Integer> loserMid) {
        if (level < MatchDao.FIRST_PLAY_OFF_MATCH_LEVEL) {
            return Optional.empty();
        }
        final Optional<Integer> mid = Optional.of(matchDao.createPlayOffMatch(
                tid, cid, parentMid, loserMid, priority, level, types.getType()));
        log.info("Play off match {}:{} of tournament {} in category {}",
                types.getType(), mid, tid, cid);
        Optional<Integer> midBronze = Optional.empty();
        if (thirdPlaceMatch && types.getType() == Gold) {
            midBronze = Optional.of(matchDao.createPlayOffMatch(
                    tid, cid, parentMid, Optional.empty(), priority, level, Brnz));
        }
        generateTree(level - 1, mid, priority - 1, types.next(), midBronze);
        generateTree(level - 1, mid, priority - 1, types.next(), midBronze);
        return mid;
    }
}
