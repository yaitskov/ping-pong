package org.dan.ping.pong.app.match.rule.filter;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MatchParticipantScope {
    @JsonProperty("alo")
    AT_LEAST_ONE {
        @Override
        public boolean isSuper(MatchParticipantScope sub) {
            return sub == AT_LEAST_ONE;
        }
    },
    BOTH {
        @Override
        public boolean isSuper(MatchParticipantScope sub) {
            return sub == BOTH;
        }
    };

    public abstract boolean isSuper(MatchParticipantScope sub);
}
