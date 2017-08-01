package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.user.UserLink;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResultEntry implements Comparable<TournamentResultEntry> {
    private UserLink user;
    private int wonMatches;
    private int score;
    private BidState state;

    @Override
    public int compareTo(TournamentResultEntry o) {
        int scoreStateThis = state.compareTo(BidState.Win1);
        int scoreStateOther = o.state.compareTo(BidState.Win1);
        if (scoreStateThis >= 0 && scoreStateOther < 0) {
            return -1;
        }
        if (scoreStateThis < 0 && scoreStateOther >= 0) {
            return 1;
        }
        int score = state.compareTo(o.state);
        if (score == 0) {
            return Integer.compare(o.wonMatches, wonMatches);
        }
        return score;
    }
}
