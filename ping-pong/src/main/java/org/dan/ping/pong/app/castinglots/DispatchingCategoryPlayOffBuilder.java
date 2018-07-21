package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.ConsoleLayered;
import static org.dan.ping.pong.app.tournament.TournamentType.Console;

import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;

import javax.inject.Inject;

public class DispatchingCategoryPlayOffBuilder implements CategoryPlayOffBuilder {
    @Inject
    private LayeredCategoryPlayOffBuilder layeredCategoryPlayOffBuilder;

    @Inject
    private FlatCategoryPlayOffBuilder flatCategoryPlayOffBuilder;

    @Override
    public void build(TournamentMemState tournament, Cid cid,
            List<ParticipantMemState> bids, DbUpdater batch) {
        if (tournament.getType() == Console
                && tournament.getRule().getCasting().getSplitPolicy() == ConsoleLayered) {
            layeredCategoryPlayOffBuilder.build(tournament, cid, bids, batch);
        } else {
            flatCategoryPlayOffBuilder.build(tournament, cid, bids, batch);
        }
    }
}
