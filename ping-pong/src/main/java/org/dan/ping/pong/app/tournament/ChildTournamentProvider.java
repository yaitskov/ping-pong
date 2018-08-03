package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.group.ConsoleTournament.NO;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConGru;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConOff;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.dan.ping.pong.app.group.ConsoleTournament;
import org.dan.ping.pong.app.tournament.console.TournamentRelationType;

import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

public class ChildTournamentProvider {
    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> relatedTidCache;

    @Inject
    private TournamentCache tournamentCache;

    public Stream<TournamentMemState> getChildren(TournamentMemState parentTournament) {
        final ConsoleTournament group = parentTournament.getRule().consoleGroup();
        final ConsoleTournament playOff = parentTournament.getRule().consolePlayOff();
        if (group == NO && playOff == NO) {
            return Stream.empty();
        }
        return childStream(loadRelations(parentTournament.getTid()), ConGru, ConOff);
    }

    public Stream<TournamentMemState> childStream(
            RelatedTids relatedTids, TournamentRelationType... types) {
        return Stream.of(types).map(relatedTids::childO)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(tournamentCache::load);
    }

    @SneakyThrows
    private RelatedTids loadRelations(Tid tid) {
        return relatedTidCache.get(tid);
    }
}
