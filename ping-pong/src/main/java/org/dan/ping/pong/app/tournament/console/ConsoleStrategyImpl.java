package org.dan.ping.pong.app.tournament.console;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.TERMINAL_STATE;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConGru;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConOff;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.CategoryService;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.sched.ScheduleServiceSelector;
import org.dan.ping.pong.app.tournament.EnlistTournament;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentCache;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

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

    @Inject
    private ScheduleServiceSelector scheduleService;

    @Inject
    private Clocker clocker;

    @Override
    @SneakyThrows
    public void onGroupComplete(
            Gid gid, TournamentMemState masterTournament,
            Set<Bid> loserBids, DbUpdater batch) {
        final TournamentMemState conGruTour = findConTour(masterTournament.getTid(), ConGru);
        final Cid cid = categoryService.findCidOrCreate(
                masterTournament, gid, conGruTour, batch);
        batch.onFailure(() -> tournamentCache.invalidate(conGruTour.getTid()));
        log.info("Enlist loser bids {} to console tournament {}",
                loserBids, conGruTour.getTid());
        loserBids.stream()
                .map(masterTournament::getParticipant)
                .forEach(bid -> enlist(bid, conGruTour, batch, cid));
        if (!areAllGroupMatchesOver(masterTournament)) {
            return;
        }
        log.info("All group matches of tid {} are over, so begin console tournament {}",
                masterTournament.getTid(), conGruTour.getTid());
        begin(masterTournament, batch, conGruTour);
    }

    public void begin(
            TournamentMemState masterTournament, DbUpdater batch, TournamentMemState conGruTour) {
        tournamentService.begin(conGruTour, batch);
        masterTournament.getCondActions().getOnScheduleTables().add(
                () -> {
                    log.info("Begin console tournament {}", conGruTour.getTid());
                    scheduleService.beginTournament(conGruTour, batch, clocker.get());
                });
    }

    private void enlist(ParticipantMemState bid, TournamentMemState tour,
            DbUpdater batch, Cid cid) {
        tournamentService.enlistToConsole(
                bid.getBid(),
                EnlistTournament.builder()
                        .categoryId(cid)
                        .bidState(
                                TERMINAL_STATE.contains(bid.getBidState())
                                        ? bid.getBidState()
                                        : Here)
                        .providedRank(Optional.empty())
                        .build(),
                tour, bid, batch);
    }

    @Override
    public void onPlayOffCategoryComplete(
            Cid cid, TournamentMemState masterTournament, DbUpdater batch) {
        final TournamentMemState conOffTour = findConTour(masterTournament.getTid(), ConOff);
        log.info("All play off matches of tid {} are over, so begin console tournament {}",
                masterTournament.getTid(), conOffTour.getTid());
        begin(masterTournament, batch, conOffTour);
    }

    @SneakyThrows
    private TournamentMemState findConTour(Tid masterTid, TournamentRelationType type) {
        final RelatedTids relatedTids = tournamentRelationCache.get(masterTid);

        return tournamentCache.load(
                ofNullable(relatedTids.getChildren().get(type))
                        .orElseThrow(() -> internalError("tournament  "
                                + masterTid + " has no " + type + " console tournament")));
    }

    @Override
    public void onParticipantLostPlayOff(
            TournamentMemState masterTournament, ParticipantMemState par, DbUpdater batch) {
        final TournamentMemState conOffTour = findConTour(masterTournament.getTid(), ConOff);
        final Cid cid = categoryService.findCidOrCreate(
                masterTournament, par.getCid(), conOffTour, batch);
        batch.onFailure(() -> tournamentCache.invalidate(conOffTour.getTid()));
        enlist(par, conOffTour, batch, cid);
    }

    private boolean areAllGroupMatchesOver(TournamentMemState tournament) {
        return !tournament.getMatches()
                .values().stream()
                .anyMatch(m -> m.getGid().isPresent() && m.getState() != Over);
    }
}
