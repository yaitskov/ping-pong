package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.ConsoleLayered;
import static org.dan.ping.pong.app.tournament.TournamentType.Console;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.console.TournamentRelationType;
import org.dan.ping.pong.app.tournament.rel.RelatedTournamentsService;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;

import javax.inject.Inject;

@Slf4j
public class DispatchingCategoryPlayOffBuilder implements CategoryPlayOffBuilder {
    @Inject
    private GroupLayeredCategoryPlayOffBuilder groupLayeredCategoryPlayOffBuilder;

    @Inject
    private PlayOffLayeredCategoryPlayOffBuilder playOffLayeredCategoryPlayOffBuilder;

    @Inject
    private FlatCategoryPlayOffBuilder flatCategoryPlayOffBuilder;

    @Inject
    private RelatedTournamentsService relatedTournaments;

    @Override
    @SneakyThrows
    public void build(TournamentMemState conTour, Cid cid,
            List<ParticipantMemState> bids, DbUpdater batch) {
        if (conTour.getType() == Console
                && conTour.getRule().getCasting().getSplitPolicy() == ConsoleLayered) {
            log.info("Layered console casting for tid {} cid {}", conTour.getTid(), cid);
            final TournamentRelationType relType = relatedTournaments
                    .findRelationTypeWithParent(conTour.getTid());
            switch (relType) {
                case ConGru:
                    groupLayeredCategoryPlayOffBuilder.build(conTour, cid, bids, batch);
                    return;
                case ConOff:
                    playOffLayeredCategoryPlayOffBuilder.build(conTour, cid, bids, batch);
                    return;
                default:
                    throw internalError("not supported relation type " + relType);
            }
        } else {
            flatCategoryPlayOffBuilder.build(conTour, cid, bids, batch);
        }
    }
}
