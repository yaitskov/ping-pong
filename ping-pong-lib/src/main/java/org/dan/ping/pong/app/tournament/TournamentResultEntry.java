package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.user.UserLink;

import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResultEntry {
    private UserLink user;
    private BidState state;
    private int punkts;
    private CumulativeScore score;
    private Optional<Integer> playOffStep;
}
