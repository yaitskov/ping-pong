package org.dan.ping.pong.app.group;

import static java.util.stream.Collectors.toList;

import org.dan.ping.pong.app.bid.BidRemover;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;

import javax.inject.Inject;

public class GroupRemover {
    @Inject
    private GroupDao groupDao;

    @Inject
    private BidRemover bidRemover;

    public void removeByCategory(TournamentMemState tournament, Cid cid, DbUpdater batch) {
        bidRemover.removeByCategory(tournament, cid, batch);

        final List<Gid> gids = tournament.getGroups()
                .values().stream()
                .filter(gi -> gi.getCid().equals(cid))
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
