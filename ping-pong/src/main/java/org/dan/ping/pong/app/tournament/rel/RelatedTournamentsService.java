package org.dan.ping.pong.app.tournament.rel;

import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.console.TournamentRelationType;

import javax.inject.Inject;
import javax.inject.Named;

public class RelatedTournamentsService {
    @Inject
    @Named(TOURNAMENT_CACHE)
    private LoadingCache<Tid, TournamentMemState> tournamentCache;

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> tournamentRelationCache;

    @SneakyThrows
    public TournamentMemState findParent(Tid childTid) {
        final RelatedTids relatedTids = tournamentRelationCache.get(childTid);
        final Tid masterTid = relatedTids.parentTidO()
                .orElseThrow(() -> internalError(
                        "tid " + childTid + " has no master tournament"));

       return tournamentCache.get(masterTid);
    }

    @SneakyThrows
    public TournamentRelationType findRelationTypeWithParent(Tid childTid) {
        final Tid masterTid = tournamentRelationCache.get(childTid).parentTid();
        return tournamentRelationCache.get(masterTid).findRelTypeByTid(childTid);
    }
}
