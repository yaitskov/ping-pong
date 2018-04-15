package org.dan.ping.pong.app.match.rule.filter;

import static org.dan.ping.pong.app.match.MatchState.Over;

import org.dan.ping.pong.app.match.MatchInfo;

import java.util.stream.Stream;

public enum MatchOutcomeScope implements FilterMarker {
    ALL_MATCHES {
        @Override
        public boolean isSuper(MatchOutcomeScope outcomeScope) {
            return true;
        }

        public Stream<MatchInfo> filterMatches(Stream<MatchInfo> s) {
            return s;
        }
    },

    JUST_NORMALLY_COMPLETE {
        @Override
        public boolean isSuper(MatchOutcomeScope outcomeScope) {
            return outcomeScope == JUST_NORMALLY_COMPLETE;
        }

        public Stream<MatchInfo> filterMatches(Stream<MatchInfo> s) {
            return s.filter(m -> m.getState() == Over);
        }
    };

    public abstract boolean isSuper(MatchOutcomeScope outcomeScope);
}
