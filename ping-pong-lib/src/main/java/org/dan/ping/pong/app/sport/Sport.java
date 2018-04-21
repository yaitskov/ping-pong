package org.dan.ping.pong.app.sport;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.SetScoreReq;
import org.dan.ping.pong.app.tournament.rules.ValidationError;
import org.dan.ping.pong.sys.error.PiPoEx;

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

    void checkWonSets(T rules, Map<Uid, Integer> uidWonSets);

    default List<SetScoreReq> expandScoreSet(T rules, SetScoreReq score) {
        throw PiPoEx.badRequest("Sport does not support countOnlySets feature");
    }

    int maxUpLimitSetsDiff(T rules);

    int maxUpLimitBallsDiff(T rules);
}
