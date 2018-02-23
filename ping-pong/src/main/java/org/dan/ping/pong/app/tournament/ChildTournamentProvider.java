package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.group.ConsoleTournament.INDEPENDENT_RULES;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.dan.ping.pong.app.group.GroupRules;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

public class ChildTournamentProvider {
    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> relatedTidCache;

    @Inject
    private TournamentCache tournamentCache;

    public Optional<TournamentMemState> getChild(TournamentMemState parentTournament) {
        return parentTournament.getRule().getGroup()
                .map(GroupRules::getConsole)
                .filter(c -> c == INDEPENDENT_RULES)
                .flatMap(gr -> loadRelations(parentTournament.getTid())
                        .getChild().map(cc -> tournamentCache.load(cc)));
    }

    @SneakyThrows
    private RelatedTids loadRelations(Tid tid) {
        return relatedTidCache.get(tid);
    }
}
