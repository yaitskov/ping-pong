package org.dan.ping.pong.app.tournament.console;

import com.google.common.cache.CacheLoader;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentDao;

import javax.inject.Inject;

public class TournamentRelationCacheLoader extends CacheLoader<Tid, RelatedTids> {
    @Inject
    private TournamentDao tournamentDao;

    @Override
    public RelatedTids load(Tid tid) throws Exception {
        return tournamentDao.getRelatedTids(tid);
    }
}
