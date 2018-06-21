package org.dan.ping.pong.app.match.rule.rules.attrs;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;

public interface ParticipantScoped {
    String MPS = "mps";

    @JsonProperty(MPS)
    MatchParticipantScope getMatchParticipantScope();
    @JsonProperty(MPS)
    void setMatchParticipantScope(MatchParticipantScope scope);
}
