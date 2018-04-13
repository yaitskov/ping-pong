package org.dan.ping.pong.app.match.rule.rules.attrs;

import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;

public interface CompletenessScoped {
    MatchOutcomeScope getMatchOutcomeScope();
    void setMatchOutcomeScope(MatchOutcomeScope scope);
}
