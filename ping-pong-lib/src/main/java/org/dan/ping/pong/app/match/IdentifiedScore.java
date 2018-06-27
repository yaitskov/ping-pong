package org.dan.ping.pong.app.match;

import static java.lang.String.format;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class IdentifiedScore {
    private Bid bid;
    private int score;

    public String toString() {
        return format("(uid=%d, score=%d", bid.intValue(), score);
    }

    public static IdentifiedScore scoreOf(Bid bid, int score) {
        return IdentifiedScore.builder().bid(bid).score(score).build();
    }
}
