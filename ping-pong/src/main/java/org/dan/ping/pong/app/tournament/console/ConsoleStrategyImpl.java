package org.dan.ping.pong.app.tournament.console;

import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.CategoryService;
import org.dan.ping.pong.app.tournament.EnlistTournament;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentCache;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class ConsoleStrategyImpl implements ConsoleStrategy {
    @Inject
    private TournamentService tournamentService;

    @Inject
    private CategoryService categoryService;

    @Inject
    private TournamentCache tournamentCache;

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    public LoadingCache<Tid, RelatedTids> tournamentRelationCache;

    private int findCidOrCreate(TournamentMemState tournament, int gid,
            TournamentMemState consoleTournament, DbUpdater batch) {
        final int masterCid = tournament.getGroup(gid).getCid();
        final String categoryName = tournament.getCategory(masterCid).getName();
        final Optional<Integer> oCid = consoleTournament.findCidByName(categoryName);
        if (oCid.isPresent()) {
            return oCid.get();
        }
        return categoryService.createCategory(consoleTournament, categoryName, batch);
    }

    @Override
    @SneakyThrows
    public void onGroupComplete(int gid, TournamentMemState tournament, Set<Uid> loserUids, DbUpdater batch) {
        final RelatedTids relatedTids = tournamentRelationCache.get(tournament.getTid());

        final TournamentMemState consoleTournament = tournamentCache.load(
                relatedTids.getChild().orElseThrow(() -> internalError("tournament  "
                        + tournament.getTid() + " has no console tournament")));


        final int cid = findCidOrCreate(tournament, gid, consoleTournament, batch);

        batch.onFailure(() -> tournamentCache.invalidate(consoleTournament.getTid()));
        log.info("Enlist loser bids {} to console tournament {}",
                loserUids, consoleTournament.getTid());
        consoleTournament.setState(Draft);
        loserUids.stream().map(tournament::getParticipant).forEach(bid ->
                        tournamentService.enlistOnlineWithoutValidation(
                                EnlistTournament.builder()
                                        .categoryId(cid)
                                        .bidState(Here)
                                        .providedRank(Optional.empty())
                                        .build(),
                                consoleTournament, bid, batch));
        if (!areAllGroupMatchesOver(tournament)) {
            return;
        }
        log.info("All group matches of tid {} are over, so begin console tournament {}",
                tournament.getTid(), consoleTournament.getTid());
        tournamentService.begin(consoleTournament, batch);
    }

    private boolean areAllGroupMatchesOver(TournamentMemState tournament) {
        return !tournament.getMatches()
                .values().stream()
                .anyMatch(m -> m.getGid().isPresent() && m.getState() != Over);
    }
}
