package org.dan.ping.pong.app.match.rule.filter;

import static org.dan.ping.pong.app.match.MatchState.Break;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dan.ping.pong.app.match.MatchInfo;

import java.util.stream.Stream;

public enum MatchOutcomeScope implements FilterMarker {
    @JsonProperty("am")
    ALL_MATCHES {
        @Override
        public boolean isSuper(MatchOutcomeScope outcomeScope) {
            return true;
        }

        public Stream<MatchInfo> filterMatches(Stream<MatchInfo> s) {
            return s;
        }
    },

    @JsonProperty("jnc")
    JUST_NORMALLY_COMPLETE {
        @Override
        public boolean isSuper(MatchOutcomeScope outcomeScope) {
            return outcomeScope == JUST_NORMALLY_COMPLETE;
        }

        public Stream<MatchInfo> filterMatches(Stream<MatchInfo> s) {
            return s.filter(m -> m.getState() != Break);
        }
    };

    public abstract boolean isSuper(MatchOutcomeScope outcomeScope);
}
