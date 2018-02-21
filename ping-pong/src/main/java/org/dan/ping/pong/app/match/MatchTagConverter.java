package org.dan.ping.pong.app.match;

import org.jooq.impl.AbstractConverter;

public class MatchTagConverter extends AbstractConverter<String, MatchTag> {
    public MatchTagConverter() {
        super(String.class, MatchTag.class);
    }

    static final MatchTag MASTER_TOURNAMENT_TAG = MatchTag.builder()
            .number(0)
            .prefix("master")
            .build();

    @Override
    public MatchTag from(String s) {
        if (s == null) {
            return MASTER_TOURNAMENT_TAG;
        }
        return MatchTag.parse(s);
    }

    @Override
    public String to(MatchTag tag) {
        if (tag.equals(MASTER_TOURNAMENT_TAG)) {
            return null;
        }
        return tag.serialize();
    }
}
