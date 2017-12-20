package org.dan.ping.pong.app.castinglots;

import static com.google.common.primitives.Ints.asList;
import static org.dan.ping.pong.app.group.GroupSchedule.oneBased;
import static org.dan.ping.pong.app.match.MatchType.Brnz;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.match.MatchType.POff;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.match.MatchType;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Builder
public class PlayOffGenerator {
    public static final Map<Integer, List<Integer>> PLAY_OFF_SEEDS = ImmutableMap
            .<Integer, List<Integer>>builder()
            .put(2, asList(0, 1))
            .put(4, asList(0, 3, 1, 2))
            .put(8, oneBased(1, 8, 5, 4, 3, 6, 7, 2))
            .put(16, oneBased(
                    1, 16, 8, 9, 5, 12, 4, 13,
                    3, 14, 6, 11, 7, 10, 2, 15))
            .put(32, oneBased(
                    1, 32, 17, 16, 9, 24, 25, 8,
                    28, 5, 12, 21, 20, 13, 4, 29,
                    3, 30, 14, 19, 11, 22, 6, 27,
                    26, 7, 23, 10, 18, 15, 2, 31))
            .put(64, oneBased(
                    1, 64, 32, 33, 17, 48, 16, 49,
                    9, 56, 24, 41, 25, 40, 8, 57,
                    5, 60, 28, 37, 21, 44, 12, 53,
                    13, 52, 45, 20, 29, 36, 4, 61,
                    3, 62, 30, 35, 19, 46, 14, 51,
                    11, 54, 22, 43, 27, 38, 6, 59,
                    7, 58, 26, 39, 23, 42,10, 55,
                    15, 50, 18, 47, 31, 34, 2, 63))
            .put(128, oneBased(
                    1, 128, 64, 65, 32, 97, 33, 96,
                    16, 113, 49, 80, 17, 112, 48, 81,
                    8, 121, 57, 72, 25, 104, 40, 89,
                    9, 120, 56, 73, 24, 105, 41, 88,
                    5, 124, 60, 69, 28, 101, 37, 92,
                    12, 117, 53, 76, 21, 108, 44, 85,
                    4, 125, 61, 68, 29, 100, 36, 93,
                    13, 116, 52, 77, 20, 109, 45, 84,
                    6, 123, 59, 70, 27, 102, 38, 91,
                    11, 118, 54, 75, 22, 107, 43, 86,
                    3, 126, 62, 67, 30, 99, 35, 94,
                    14, 115, 51, 78, 19, 110, 46, 83,
                    7, 122, 58, 71, 26, 103, 39, 90,
                    10, 119, 55, 74, 23, 106, 42, 87,
                    2, 127, 63, 66, 31, 98, 34, 95,
                    15, 114, 50, 79, 18, 111, 47, 82))
            .build();
    private static final int FIRST_PLAY_OFF_MATCH_LEVEL = 1;
    public static final Mid MID0 = new Mid(0);

    private final TournamentMemState tournament;
    private final int cid;
    private final boolean thirdPlaceMatch;
    private final MatchDao matchDao;

    public Optional<Mid> generateTree(int level, Optional<Mid> parentMid,
            int priority, TypeChain types, Optional<Mid> loserMid) {
        if (level < FIRST_PLAY_OFF_MATCH_LEVEL) {
            return Optional.empty();
        }
        final Optional<Mid> omid = createMatch(parentMid, loserMid,
                priority + 1, level, types.getType());
        Optional<Mid> midBronze = Optional.empty();
        if (thirdPlaceMatch && types.getType() == Gold && level > 1) {
            midBronze = createMatch(Optional.empty(), Optional.empty(),
                    priority, level, Brnz);
        }
        generateTree(level - 1, omid, priority - 1, types.next(), midBronze);
        generateTree(level - 1, omid, priority - 1, types.next(), midBronze);
        return omid;
    }

    private Optional<Mid> createMatch(Optional<Mid> winMid, Optional<Mid> loserMid,
            int priority, int level, MatchType type) {
        final Mid mid = matchDao.createPlayOffMatch(
                tournament.getTid(), cid, winMid, loserMid, priority, level, type);
        final Optional<Mid> omid = Optional.of(mid);
        tournament.getMatches().put(mid, MatchInfo.builder()
                .tid(tournament.getTid())
                .mid(mid)
                .state(MatchState.Draft)
                .priority(priority)
                .level(level)
                .gid(Optional.empty())
                .participantIdScore(new HashMap<>())
                .type(type)
                .winnerMid(winMid)
                .loserMid(loserMid)
                .cid(cid)
                .build());
        log.info("playoff {} mid {} of tid {} in cid {}",
                type, omid.orElse(MID0), tournament.getTid(), cid);
        return omid;
    }

    public Optional<Mid> generate2LossTree(int level/*8*/, int priority) {
        final Optional<Mid> omid = createMatch(Optional.empty(), Optional.empty(), priority, level, Gold);
        List<Mid> lostRefMids = new ArrayList<>();
        omid.ifPresent(lostRefMids::add);
        final List<Mid> nextMidLevel = new ArrayList<>();
        level -= 1; // 7
        createMatchForBronze(level, priority, lostRefMids, nextMidLevel);
        if (level == 1) {
            return omid;
        }
        while (true) {
            final List<Mid> favorites = new ArrayList<>();
            final List<Mid> newLostRefs = new ArrayList<>();
            level -= 1;//6 //4
            for (int i = 0; i < nextMidLevel.size(); ++i) {
                createWinMatch(level + 1, priority, lostRefMids, nextMidLevel, favorites, i);
                createLoseMatch(level, priority, nextMidLevel, newLostRefs, i);
            }
            level -= 1;
            if (level == 1) {
                log.info("base");
                createBaseMatches(level, priority, favorites, newLostRefs);
                break;
            }
            nextMidLevel.clear();
            createJoinLevelMatchesForLosers(level/*5*/, priority, nextMidLevel, newLostRefs);
            lostRefMids = favorites;
        }
        return omid;
    }

    private void createLoseMatch(int level, int priority, List<Mid> nextMidLevel, List<Mid> newLostRefs, int i) {
        createMatch(Optional.of(nextMidLevel.get(nextMidLevel.size() - 1 - i)),
                Optional.empty(), priority, level, POff)
                .ifPresent(newLostRefs::add);
        log.info("{} => {}  | ",
                newLostRefs.get(newLostRefs.size() - 1), nextMidLevel.get(nextMidLevel.size() - 1 - i));
    }

    private void createWinMatch(int level, int priority, List<Mid> lostRefMids, List<Mid> nextMidLevel, List<Mid> favorites, int i) {
        createMatch(Optional.of(lostRefMids.get(i / 2)),
                Optional.of(nextMidLevel.get(nextMidLevel.size() - 1 - i)),
                priority, level, POff)
                .ifPresent(favorites::add);
        log.info("{} => {}  | {} => {}",
                favorites.get(favorites.size() - 1), lostRefMids.get(i / 2),
                favorites.get(favorites.size() - 1), nextMidLevel.get(nextMidLevel.size() - 1 - i));
    }

    private void createJoinLevelMatchesForLosers(int level, int priority, List<Mid> nextMidLevel, List<Mid> newLostRefs) {
        for (int i = newLostRefs.size() - 1;  i >= 0; --i) {
            final Mid lostMid = newLostRefs.get(i);
            createMatch(Optional.of(lostMid),
                    Optional.empty(),
                    priority, level, POff)
                    .ifPresent(nextMidLevel::add);
            log.info("{} => {}  | ",
                    nextMidLevel.get(nextMidLevel.size() - 1), lostMid);
            createMatch(Optional.of(lostMid),
                    Optional.empty(),
                    priority, level, POff)
                    .ifPresent(nextMidLevel::add);
            log.info("{} => {}  | ",
                    nextMidLevel.get(nextMidLevel.size() - 1), lostMid);
        }
    }

    private void createMatchForBronze(int level, int priority,
            List<Mid> lostRefMids, List<Mid> nextMidLevel) {
        for (Mid mid : lostRefMids) {
            createMatch(Optional.of(mid),
                    level == 1
                            ? Optional.of(mid)
                            : Optional.empty(),
                    priority, level,
                    level == 1 ? POff : Brnz)
                    .ifPresent(nextMidLevel::add);
        }
    }

    private void createBaseMatches(int level, int priority,
            List<Mid> favorites, List<Mid> newLostRefs) {
        for (int i = 0; i < newLostRefs.size(); ++i) {
            final Mid lostMid = newLostRefs.get(newLostRefs.size() - i - 1);
            final int jj = i;
            createMatch(Optional.of(favorites.get(i)),
                    Optional.of(lostMid),
                    priority, level, POff)
                    .ifPresent(mid -> log.info("{} => {}  | {} => {}",
                            mid, favorites.get(jj),
                            mid, lostMid));

            createMatch(Optional.of(favorites.get(i)),
                    Optional.of(lostMid),
                    priority, level, POff)
                    .ifPresent(mid -> log.info("{} => {}  | {} => {}",
                            mid, favorites.get(jj),
                            mid, lostMid));
        }
    }
}
