package org.dan.ping.pong.app.match;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
public class MatchTag {
    public static final String CONSOLE_LEVEL = "L";
    public static final String DISAMBIGUATION = "DG";

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
