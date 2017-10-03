package org.dan.ping.pong.app.server.match;

import static java.lang.String.format;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentifiedScore {
    private int uid;
    private int score;

    public String toString() {
        return format("(uid=%d, score=%d", uid, score);
    }
}
