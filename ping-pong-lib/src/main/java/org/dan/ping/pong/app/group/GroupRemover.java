package org.dan.ping.pong.app.group;

import static java.util.stream.Collectors.toList;

import org.dan.ping.pong.app.bid.BidRemover;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;

import javax.inject.Inject;

public class GroupRemover {
    @Inject
    private GroupDao groupDao;

    @Inject
    private BidRemover bidRemover;

    public void removeByCategory(TournamentMemState tournament, int cid, DbUpdater batch) {
        bidRemover.removeByCategory(tournament, cid, batch);

        final List<Integer> gids = tournament.getGroups()
                .values().stream()
                .filter(gi -> gi.getCid() == cid)
                .map(GroupInfo::getGid)
                .collect(toList());

        groupDao.deleteByIds(batch, tournament.getTid(), gids);
        tournament.getGroups().keySet().removeAll(gids);
    }

    public void removeByTournament(TournamentMemState tournament, DbUpdater batch) {
        groupDao.deleteAllByTid(tournament.getTid(), batch, tournament.getGroups().size());
        tournament.getGroups().clear();
    }
}
