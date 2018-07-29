package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.ConsoleLayered;
import static org.dan.ping.pong.app.tournament.TournamentType.Console;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.category.SelectedCid;
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
    public void build(SelectedCid sCid, List<ParticipantMemState> bids) {
        if (sCid.tourType() == Console
                && sCid.casting().getSplitPolicy() == ConsoleLayered) {
            log.info("Layered console casting for tid {} cid {}", sCid.tid(), sCid.cid());
            final TournamentRelationType relType = relatedTournaments
                    .findRelationTypeWithParent(sCid.tid());
            switch (relType) {
                case ConGru:
                    groupLayeredCategoryPlayOffBuilder.build(sCid, bids);
                    return;
                case ConOff:
                    playOffLayeredCategoryPlayOffBuilder.build(sCid, bids);
                    return;
                default:
                    throw internalError("not supported relation type " + relType);
            }
        } else {
            flatCategoryPlayOffBuilder.build(sCid, bids);
        }
    }
}
