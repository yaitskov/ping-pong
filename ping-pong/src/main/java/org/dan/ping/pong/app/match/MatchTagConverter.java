package org.dan.ping.pong.app.match;

import org.jooq.impl.AbstractConverter;

public class MatchTagConverter extends AbstractConverter<String, MatchTag> {
    public MatchTagConverter() {
        super(String.class, MatchTag.class);
    }

    @Override
    public MatchTag from(String s) {
        if (s == null) {
            return null; //MASTER_TOURNAMENT_TAG;
        }
        return MatchTag.parse(s);
    }

    @Override
    public String to(MatchTag tag) {
        if (tag == null) { //.equals(MASTER_TOURNAMENT_TAG)) {
            return null;
        }
        return tag.serialize();
    }
}
