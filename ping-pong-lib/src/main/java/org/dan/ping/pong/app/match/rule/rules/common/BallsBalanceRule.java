package org.dan.ping.pong.app.match.rule.rules.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.BallsBalance;

import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;

@Getter
@Setter
public class BallsBalanceRule implements GroupOrderRule {
    private MatchParticipantScope matchParticipantScope = MatchParticipantScope.AT_LEAST_ONE;
    private MatchOutcomeScope matchOutcomeScope = MatchOutcomeScope.ALL;

    @Override
    public OrderRuleName name() {
        return BallsBalance;
    }
}
