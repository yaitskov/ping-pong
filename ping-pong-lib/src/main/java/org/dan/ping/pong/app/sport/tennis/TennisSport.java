package org.dan.ping.pong.app.sport.tennis;

import static org.dan.ping.pong.app.sport.SportType.Tennis;

import com.fasterxml.jackson.databind.JsonNode;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.sport.MatchRules;
import org.dan.ping.pong.app.sport.Sport;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TennisSport implements Sport<TennisMatchRules> {
    @Override
    public void validate(TennisMatchRules rules) {
        throw new IllegalStateException();
    }

    @Override
    public void validate(TennisMatchRules rules, MatchInfo match) {
        throw new IllegalStateException();
    }

    @Override
    public MatchRules parse(JsonNode node) {
        return null;
    }

    @Override
    public SportType getType() {
        return Tennis;
    }

    public Map<Uid, Integer> calcWonSets(Map<Uid, List<Integer>> participantScores) {
        throw new IllegalStateException();
    }

    public Optional<Uid> findWinnerId(TennisMatchRules rules, Map<Uid, Integer> wonSets) {
        throw new IllegalStateException();
    }

    @Override
    public Optional<Uid> findStronger(Map<Uid, Integer> wonSets) {
        throw new IllegalStateException();
    }

    public void checkWonSets(PingPongMatchRules rules, Map<Uid, Integer> uidWonSets) {
        throw new IllegalStateException();
    }
}
