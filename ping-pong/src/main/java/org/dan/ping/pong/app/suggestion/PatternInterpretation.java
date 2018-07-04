package org.dan.ping.pong.app.suggestion;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PatternInterpretation {
    private SuggestionIndexType idxType;
    private String pattern;
    private boolean like;
}
