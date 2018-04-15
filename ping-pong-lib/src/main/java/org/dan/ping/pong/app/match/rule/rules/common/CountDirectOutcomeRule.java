package org.dan.ping.pong.app.match.rule.rules.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.F2F;
import static org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope.ALL_MATCHES;
import static org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope.BOTH;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;

@Getter
@Setter
public class CountDirectOutcomeRule implements GroupOrderRule {
    public static final CountDirectOutcomeRule COUNT_DIRECT_OUTCOME_RULE
            = new CountDirectOutcomeRule();

    private MatchOutcomeScope matchOutcomeScope = ALL_MATCHES;

    @Override
    public OrderRuleName name() {
        return F2F;
    }

    @Override
    @JsonIgnore
    public MatchParticipantScope getMatchParticipantScope() {
        return BOTH;
    }

    @Override
    public void setMatchParticipantScope(MatchParticipantScope scope) {
        // drop
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + matchOutcomeScope + ")";
    }
}
