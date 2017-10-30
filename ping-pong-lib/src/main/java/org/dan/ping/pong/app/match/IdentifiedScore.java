package org.dan.ping.pong.app.match;

import static java.lang.String.format;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.Uid;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentifiedScore {
    private Uid uid;
    private int score;

    public String toString() {
        return format("(uid=%d, score=%d", uid.getId(), score);
    }
}