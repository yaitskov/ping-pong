package org.dan.ping.pong.app.match.rule.rules.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpWeightedMatches;

import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;
import org.dan.ping.pong.app.match.rule.rules.AbstractGroupOrderRule;

@Getter
@Setter
public class AtpWeightedMatchesBySetsRule extends AbstractGroupOrderRule {
    private MatchParticipantScope matchParticipantScope = MatchParticipantScope.BOTH;
    private MatchOutcomeScope matchOutcomeScope = MatchOutcomeScope.ALL_MATCHES;

    @Override
    public OrderRuleName name() {
        return AtpWeightedMatches;
    }
}
