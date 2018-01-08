package org.dan.ping.pong.app.sport;

import com.fasterxml.jackson.databind.JsonNode;
import org.dan.ping.pong.app.match.MatchInfo;

public interface Sport<T extends MatchRules> {
    void validate(T rules);

    void validate(T rules, MatchInfo match);

    MatchRules parse(JsonNode node);

    SportType getType();
}
