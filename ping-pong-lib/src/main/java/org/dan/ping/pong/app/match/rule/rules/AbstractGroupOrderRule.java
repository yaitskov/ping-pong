package org.dan.ping.pong.app.match.rule.rules;

import static java.lang.String.format;

public abstract class AbstractGroupOrderRule implements GroupOrderRule {
    public String toString() {
        return format("%s(%s, %s)", this.getClass().getSimpleName(),
                getMatchOutcomeScope(), getMatchParticipantScope());
    }
}
