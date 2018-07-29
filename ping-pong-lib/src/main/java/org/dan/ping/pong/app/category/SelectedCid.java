package org.dan.ping.pong.app.category;

import lombok.Builder;
import lombok.Getter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.playoff.PlayOffRule;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.tournament.TournamentType;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Optional;

@Getter
@Builder
public class SelectedCid {
    private final Cid cid;
    private final TournamentMemState tournament;
    private final DbUpdater batch;

    public static SelectedCid selectCid(
            Cid cid, TournamentMemState tournament, DbUpdater batch) {
        return SelectedCid.builder()
                .cid(cid)
                .tournament(tournament)
                .batch(batch)
                .build();
    }

    public Tid tid() {
        return tournament.getTid();
    }

    public DbUpdater batch() {
        return batch;
    }

    public CategoryMemState category() {
        return tournament.getCategory(cid);
    }

    public Cid cid() {
        return cid;
    }

    public TournamentMemState tournament() {
        return tournament;
    }

    public TournamentType tourType() {
        return tournament.getType();
    }

    public CastingLotsRule casting() {
        return tournament.casting();
    }

    public ParticipantMemState getBid(Bid bid) {
        return tournament.getBid(bid);
    }

    public Optional<PlayOffRule> playOff() {
        return tournament.getRule().getPlayOff();
    }

    public TournamentRules rules() {
        return tournament.getRule();
    }
}
