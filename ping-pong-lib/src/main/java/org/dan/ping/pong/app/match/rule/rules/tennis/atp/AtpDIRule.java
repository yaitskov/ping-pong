package org.dan.ping.pong.app.match.rule.rules.tennis.atp;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpDI;
import static org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope.AT_LEAST_ONE;
import static org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope.BOTH;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;
import org.dan.ping.pong.app.match.rule.rules.AbstractGroupOrderRule;

@Getter
@Setter
public class AtpDIRule extends AbstractGroupOrderRule {
    @Override
    public OrderRuleName name() {
        return AtpDI;
    }

    @Override
    @JsonIgnore
    public MatchParticipantScope getMatchParticipantScope() {
        return AT_LEAST_ONE;
    }

    @Override
    public void setMatchParticipantScope(MatchParticipantScope scope) {
        // drop
    }

    @Override
    @JsonIgnore
    public MatchOutcomeScope getMatchOutcomeScope() {
        return MatchOutcomeScope.ALL_MATCHES;
    }

    @Override
    public void setMatchOutcomeScope(MatchOutcomeScope scope) {

    }
}
