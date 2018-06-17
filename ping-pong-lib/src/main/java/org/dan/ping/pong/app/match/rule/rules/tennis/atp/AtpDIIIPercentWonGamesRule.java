package org.dan.ping.pong.app.match.rule.rules.tennis.atp;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpDIII;
import static org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope.ALL_MATCHES;
import static org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope.AT_LEAST_ONE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;
import org.dan.ping.pong.app.match.rule.rules.AbstractGroupOrderRule;

@Getter
@Setter
public class AtpDIIIPercentWonGamesRule extends AbstractGroupOrderRule {
    public static final AtpDIIIPercentWonGamesRule ATP_D3
            = new AtpDIIIPercentWonGamesRule();

    @Override
    public OrderRuleName name() {
        return AtpDIII;
    }

    @Override
    @JsonIgnore
    public MatchOutcomeScope getMatchOutcomeScope() {
        return ALL_MATCHES;
    }

    @Override
    public void setMatchOutcomeScope(MatchOutcomeScope scope) {
        // SKIP
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
