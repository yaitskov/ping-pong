package org.dan.ping.pong.app.match.rule.rules.attrs;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;

public interface CompletenessScoped {
    String MOS = "mos";

    @JsonProperty(MOS)
    MatchOutcomeScope getMatchOutcomeScope();
    @JsonProperty(MOS)
    void setMatchOutcomeScope(MatchOutcomeScope scope);
}
