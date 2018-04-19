package org.dan.ping.pong.app.match.rule.rules.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostBalls;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostSets;

import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;
import org.dan.ping.pong.app.match.rule.rules.AbstractGroupOrderRule;

@Getter
@Setter
public class LostSetsRule extends AbstractGroupOrderRule {
    private MatchParticipantScope matchParticipantScope = MatchParticipantScope.AT_LEAST_ONE;
    private MatchOutcomeScope matchOutcomeScope = MatchOutcomeScope.ALL_MATCHES;

    @Override
    public OrderRuleName name() {
        return LostSets;
    }
}
