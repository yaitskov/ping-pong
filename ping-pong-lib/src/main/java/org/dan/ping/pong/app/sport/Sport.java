package org.dan.ping.pong.app.sport;

import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.tournament.rules.ValidationError;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Sport<T extends MatchRules> {
    void validate(Multimap<String, ValidationError> errors, T rules);

    void validate(T rules, MatchInfo match);

    SportType getType();

    Map<Uid, Integer> calcWonSets(Map<Uid, List<Integer>> participantScores);

    Optional<Uid> findWinnerId(T rules, Map<Uid, Integer> wonSets);

    Optional<Uid> findStronger(Map<Uid, Integer> wonSets);

    void checkWonSets(T rules, Map<Uid, Integer> uidWonSets);
}
