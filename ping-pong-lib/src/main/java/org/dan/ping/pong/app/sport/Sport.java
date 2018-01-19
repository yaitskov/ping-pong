package org.dan.ping.pong.app.sport;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.tournament.rules.ValidationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Sport<T extends MatchRules> {
    void validate(Multimap<String, ValidationError> errors, T rules);

    void validate(T rules, MatchInfo match);

    SportType getType();

    default Map<Uid, Integer> calcWonSets(Map<Uid, List<Integer>> participantScores) {
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

    Optional<Uid> findWinnerId(T rules, Map<Uid, Integer> wonSets);

    default Optional<Uid> findStronger(Map<Uid, Integer> wonSets) {
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

    void checkWonSets(T rules, Map<Uid, Integer> uidWonSets);
}
