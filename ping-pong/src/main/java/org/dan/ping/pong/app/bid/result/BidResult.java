package org.dan.ping.pong.app.bid.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.group.BidSuccessInGroup;
import org.dan.ping.pong.app.tournament.TournamentLink;
import org.dan.ping.pong.app.user.UserLink;

import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResult {
    private TournamentLink tournament;
    private UserLink user;
    private BidState state;
    private Optional<Integer> position;
    private Optional<BidSuccessInGroup> normal;
    private Optional<BidMatchesStat> matches;
    private Optional<BidTimeStats> time;
    private Optional<UserLink> beated = Optional.empty();
    private Optional<UserLink> conceded = Optional.empty();

    public static class BidResultBuilder {
        Optional<UserLink> beated = Optional.empty();
        Optional<UserLink> conceded = Optional.empty();
    }
}
