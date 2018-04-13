package org.dan.ping.pong.app.match.rule.rules.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.Random;
import static org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope.ALL;
import static org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope.AT_LEAST_ONE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;

public class PickRandomlyRule implements GroupOrderRule {
    public static final PickRandomlyRule PICK_RANDOMLY_RULE = new PickRandomlyRule();

    @Override
    public OrderRuleName name() {
        return Random;
    }

    @Override
    @JsonIgnore
    public MatchOutcomeScope getMatchOutcomeScope() {
        return ALL;
    }

    @Override
    public void setMatchOutcomeScope(MatchOutcomeScope scope) {
        // skip
    }

    @Override
    @JsonIgnore
    public MatchParticipantScope getMatchParticipantScope() {
        return AT_LEAST_ONE;
    }

    @Override
    public void setMatchParticipantScope(MatchParticipantScope scope) {
        // skip
    }
}
