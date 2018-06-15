package org.dan.ping.pong.app.match.rule.rules.tennis.atp;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpDII;
import static org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope.ALL_MATCHES;
import static org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope.AT_LEAST_ONE;

import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;
import org.dan.ping.pong.app.match.rule.rules.AbstractGroupOrderRule;

@Getter
@Setter
public class AtpDIIHighestPercentWonSetsRule extends AbstractGroupOrderRule {
    private MatchParticipantScope matchParticipantScope = AT_LEAST_ONE;
    private MatchOutcomeScope matchOutcomeScope = ALL_MATCHES;

    @Override
    public OrderRuleName name() {
        return AtpDII;
    }
}
