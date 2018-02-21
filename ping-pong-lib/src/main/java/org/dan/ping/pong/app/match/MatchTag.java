package org.dan.ping.pong.app.match;

import static java.lang.String.format;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class MatchTag {
    private String prefix;
    private int number;

    public String toString() {
        return serialize();
    }

    public String serialize() {
        return format("%s%d", prefix, number);
    }

    public static MatchTag parse(String s) {
        return MatchTag.builder()
                .prefix(s.substring(0, 1))
                .number(Integer.parseInt(s.substring(1)))
                .build();
    }
}
