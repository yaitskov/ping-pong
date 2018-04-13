package org.dan.ping.pong.app.match.rule.rules.attrs;

import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;

public interface ParticipantScoped {
    MatchParticipantScope getMatchParticipantScope();
    void setMatchParticipantScope(MatchParticipantScope scope);
}
