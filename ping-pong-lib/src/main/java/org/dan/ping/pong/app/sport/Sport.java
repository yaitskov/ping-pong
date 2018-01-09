package org.dan.ping.pong.app.sport;

import com.fasterxml.jackson.databind.JsonNode;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Sport<T extends MatchRules> {
    void validate(T rules);

    void validate(T rules, MatchInfo match);

    MatchRules parse(JsonNode node);

    SportType getType();

    Map<Uid, Integer> calcWonSets(Map<Uid, List<Integer>> participantScores);

    Optional<Uid> findWinnerId(T rules, Map<Uid, Integer> wonSets);

    Optional<Uid> findStronger(Map<Uid, Integer> wonSets);
}
