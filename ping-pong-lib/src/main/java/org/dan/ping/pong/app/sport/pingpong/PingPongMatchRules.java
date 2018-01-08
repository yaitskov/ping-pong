package org.dan.ping.pong.app.sport.pingpong;

import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.sport.MatchRules;

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
public class PingPongMatchRules implements MatchRules {
    private int minGamesToWin;
    private int minAdvanceInGames;
    private int minPossibleGames;
    private int setsToWin;

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

    public Optional<Uid> findStronger(MatchInfo mInfo) {
        return findStronger(calcWonSets(mInfo.getParticipantIdScore()));
    }

    public Optional<Uid> findStronger(Map<Uid, Integer> wonSets) {
        if (wonSets.size() == 1) {
            return wonSets.keySet().stream().findFirst();
        }
        final List<Uid> uids = new ArrayList<>(wonSets.keySet());
        final int scoreA = wonSets.get(uids.get(0));
        final int scoreB = wonSets.get(uids.get(1));
        if (scoreA < scoreB) {
            return Optional.of(uids.get(1));
        } else if (scoreA > scoreB) {
            return Optional.of(uids.get(0));
        } else {
            return Optional.empty();
        }
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
