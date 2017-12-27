package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.bid.Uid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Wither
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchValidationRule {
    public static final String SET = "set";
    public static final String MIN_POSSIBLE_GAMES = "minPossibleGames";
    private static final String MIN_GAMES_TO_WIN = "minGamesToWin";

    private int minGamesToWin;
    private int minAdvanceInGames;
    private int minPossibleGames;
    private int setsToWin;

    public void validateSet(int iset, List<IdentifiedScore> setScore) {
        int aGames = setScore.get(0).getScore();
        int bGames = setScore.get(1).getScore();
        int maxGames = Math.max(aGames, bGames);
        int minGames = Math.min(aGames, bGames);
        if (minGames < minPossibleGames) {
            throw badRequest("Games cannot be less than",
                    ImmutableMap.of(SET, iset,
                            MIN_POSSIBLE_GAMES, minPossibleGames));
        }
        if (maxGames < minGamesToWin) {
            throw badRequest("Winner should have at least n games",
                    ImmutableMap.of(SET, iset,
                            MIN_GAMES_TO_WIN, minGamesToWin));
        }
        if (maxGames - minGames < minAdvanceInGames) {
            throw badRequest("Difference between games cannot be less than",
                    ImmutableMap.of(SET, iset,
                            "minAdvanceInGames", minAdvanceInGames));
        }
        if (maxGames > minGamesToWin && maxGames - minGames > minAdvanceInGames) {
            throw badRequest("Winner games are to big", SET, iset);
        }
    }

    public Map<Uid, Integer> calcWonSets(Map<Uid, List<Integer>> participantScores) {
        final List<Uid> uids = new ArrayList<>(participantScores.keySet());
        if (uids.isEmpty()) {
            return Collections.emptyMap();
        }
        final Uid uidA = uids.get(0);
        if (uids.size() == 1) {
            return ImmutableMap.of(uidA, 0);
        }
        final List<Integer> setsA = participantScores.get(uidA);
        final Uid uidB = uids.get(1);
        final List<Integer> setsB = participantScores.get(uidB);
        int wonsA = 0;
        int wonsB = 0;
        for (int i = 0; i < setsA.size(); ++i) {
            if (setsA.get(i) > setsB.get(i)) {
                ++wonsA;
            } else {
                ++wonsB;
            }
        }
        return ImmutableMap.of(uidA, wonsA, uidB, wonsB);
    }

    public Optional<Uid> findWinner(MatchInfo minfo) {
        return findWinnerByScores(minfo.getParticipantIdScore());
    }

    public Optional<Uid> findWinnerByScores(Map<Uid, List<Integer>> participantScores) {
        return findWinnerId(calcWonSets(participantScores));
    }

    public Optional<Uid> findWinnerId(Map<Uid, Integer> wonSets) {
        return wonSets.entrySet().stream()
                .filter(e -> e.getValue() >= setsToWin)
                .map(Map.Entry::getKey)
                .findAny();
    }

    public void checkWonSets(Map<Uid, Integer> uidWonSets) {
        final Collection<Integer> wonSets = uidWonSets.values();
        wonSets.stream()
                .filter(n -> n > getSetsToWin()).findAny()
                .ifPresent(o -> {
                    throw badRequest("won sets more that required");
                });
        final long winners = wonSets.stream()
                .filter(n -> n == getSetsToWin())
                .count();
        if (winners > 1) {
            throw badRequest("winners are more that 1");
        }
    }
}
