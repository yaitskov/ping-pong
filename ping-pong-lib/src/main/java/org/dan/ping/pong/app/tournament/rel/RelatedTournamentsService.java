package org.dan.ping.pong.app.tournament.rel;

import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.console.TournamentRelationType;

import java.util.stream.Stream;

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
    public TournamentGroup groupOfConTours(TournamentMemState mTour) {
        final RelatedTids relatedTids = tournamentRelationCache.get(mTour.getTid());
        return TournamentGroup.builder()
                .masterTid(mTour.getTid())
                .relatedTids(relatedTids)
                .tournamentMap(Stream.concat(Stream.of(mTour),
                        relatedTids.getChildren().values()
                                .stream()
                                .map(this::loadTournament))
                        .collect(toMap(TournamentMemState::getTid, o -> o)))
                .build();
    }

    @SneakyThrows
    public TournamentRelationType findRelationTypeWithParent(Tid childTid) {
        final Tid masterTid = masterTid(childTid);
        return getRelType(childTid, masterTid);
    }

    @SneakyThrows
    public TournamentRelationType getRelType(Tid childTid, Tid masterTid) {
        return tournamentRelationCache.get(masterTid).findRelTypeByTid(childTid);
    }

    @SneakyThrows
    public Tid masterTid(Tid childTid) {
        return tournamentRelationCache.get(childTid).parentTid();
    }

    @SneakyThrows
    public TournamentMemState loadTournament(Tid tid) {
        return tournamentCache.get(tid);
    }
}
