package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.group.ConsoleTournament.NO;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConGru;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConOff;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.dan.ping.pong.app.group.ConsoleTournament;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.playoff.PlayOffRule;

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
        final ConsoleTournament group = parentTournament.getRule()
                .getGroup()
                .map(GroupRules::getConsole)
                .orElse(NO);
        final ConsoleTournament playOff = parentTournament.getRule()
                .getPlayOff()
                .map(PlayOffRule::getConsole)
                .orElse(NO);
        if (group == NO && playOff == NO) {
            return Stream.empty();
        }
        final RelatedTids relatedTids = loadRelations(parentTournament.getTid());
        if (group == NO) {
            return Stream.of(tournamentCache.load(
                    relatedTids.getChildren().get(ConOff)));
        } else if (playOff == NO) {
            return Stream.of(tournamentCache.load(
                    relatedTids.getChildren().get(ConGru)));
        }
        return Stream.of(
                tournamentCache.load(
                        relatedTids.getChildren().get(ConGru)),
                tournamentCache.load(
                        relatedTids.getChildren().get(ConOff)));
    }

    @SneakyThrows
    private RelatedTids loadRelations(Tid tid) {
        return relatedTidCache.get(tid);
    }
}
