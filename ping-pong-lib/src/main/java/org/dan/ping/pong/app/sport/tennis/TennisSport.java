package org.dan.ping.pong.app.sport.tennis;

import static org.dan.ping.pong.app.sport.SportType.Tennis;

import com.fasterxml.jackson.databind.JsonNode;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.sport.MatchRules;
import org.dan.ping.pong.app.sport.Sport;
import org.dan.ping.pong.app.sport.SportType;

public class TennisSport implements Sport<TennisMatchRules> {
    @Override
    public void validate(TennisMatchRules rules) {

    }

    @Override
    public void validate(TennisMatchRules rules, MatchInfo match) {

    }

    @Override
    public MatchRules parse(JsonNode node) {
        return null;
    }

    @Override
    public SportType getType() {
        return Tennis;
    }
}
