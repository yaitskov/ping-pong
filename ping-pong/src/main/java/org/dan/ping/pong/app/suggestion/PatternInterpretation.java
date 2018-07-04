package org.dan.ping.pong.app.suggestion;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class PatternInterpretation {
    private SuggestionIndexType idxType;
    private String pattern;
    private boolean like;
}
