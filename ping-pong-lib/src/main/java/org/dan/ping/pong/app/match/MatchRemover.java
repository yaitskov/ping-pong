package org.dan.ping.pong.app.match;

import static java.util.stream.Collectors.toList;

import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class MatchRemover {
    @Inject
    private MatchDao matchDao;

    public void deleteByMids(TournamentMemState tournament,
            DbUpdater batch, Collection<Mid> mids) {
        matchDao.deleteByIds(mids, batch);
        tournament.getMatches().keySet().removeAll(mids);
    }

    public void removeByCategory(
            TournamentMemState tournament, int cid, DbUpdater batch) {
        final List<Mid> toBeRemoved = tournament
                .findMatchesByCid(cid)
                .map(MatchInfo::getMid)
                .collect(toList());

        deleteByMids(tournament, batch, toBeRemoved);
    }

    public void removeByTournament(TournamentMemState tournament, DbUpdater batch) {
        matchDao.deleteAllByTid(tournament, batch, tournament.getMatches().size());
        tournament.getMatches().clear();
    }
}
