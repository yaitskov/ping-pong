package org.dan.ping.pong.app.match.rule.filter;

public enum MatchParticipantScope {
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
