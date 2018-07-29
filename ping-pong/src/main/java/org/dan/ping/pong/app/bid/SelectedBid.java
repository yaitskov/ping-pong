package org.dan.ping.pong.app.bid;

import lombok.Builder;
import lombok.Getter;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.sys.db.DbUpdater;

@Getter
@Builder
public class SelectedBid {
    private final ParticipantMemState bid;
    private final TournamentMemState tournament;
    private final DbUpdater batch;

    public static SelectedBid selectBid(
            TournamentMemState tournament, Bid bid, DbUpdater batch) {
        return selectBid(tournament, tournament.getParticipant(bid), batch);
    }

    public static SelectedBid selectBid(
            TournamentMemState tournament, ParticipantMemState bid, DbUpdater batch) {
        return SelectedBid
                .builder()
                .tournament(tournament)
                .bid(bid)
                .batch(batch)
                .build();
    }

    public Bid bid() {
        return bid.getBid();
    }

    public BidState bidState() {
        return bid.getBidState();
    }

    public TournamentState tidState() {
        return tournament.getState();
    }

    public Tid tid() {
        return tournament.getTid();
    }

    public Uid uid() {
        return bid.getUid();
    }
}
