package org.dan.ping.pong.app.match.rule.filter;

import static org.dan.ping.pong.app.match.MatchTag.DISAMBIGUATION;

import org.dan.ping.pong.app.match.MatchInfo;

import java.util.stream.Stream;

public enum DisambiguationScope implements FilterMarker {
    ORIGIN_MATCHES {
        @Override
        public boolean isSuper(DisambiguationScope disambiguationScope) {
            return disambiguationScope == ORIGIN_MATCHES;
        }

        @Override
        public Stream<MatchInfo> filterMatches(Stream<MatchInfo> s) {
            return s.filter(m -> !m.getTag().isPresent());
        }
    },

    DISAMBIGUATION_MATCHES {
        @Override
        public boolean isSuper(DisambiguationScope disambiguationScope) {
            return disambiguationScope == DISAMBIGUATION_MATCHES;
        }

        @Override
        public Stream<MatchInfo> filterMatches(Stream<MatchInfo> s) {
            return s.filter(m -> m.getTag()
                    .map(t -> DISAMBIGUATION.equals(t.getPrefix()))
                    .orElse(false));
        }
    };

    public abstract boolean isSuper(DisambiguationScope disambiguationScope);
}
