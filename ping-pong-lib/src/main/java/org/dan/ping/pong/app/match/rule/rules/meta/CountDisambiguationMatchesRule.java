package org.dan.ping.pong.app.match.rule.rules.meta;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.CountDisambiguationMatches;
import static org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope.ALL;
import static org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope.AT_LEAST_ONE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.sport.MatchRules;

@Getter
@Setter
public class CountDisambiguationMatchesRule implements GroupOrderRule {
    private MatchRules matchRules;

    @Override
    public OrderRuleName name() {
        return CountDisambiguationMatches;
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
