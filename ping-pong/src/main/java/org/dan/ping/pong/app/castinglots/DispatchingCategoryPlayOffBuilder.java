package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.ConsoleLayered;
import static org.dan.ping.pong.app.playoff.ConsoleLayersPolicy.IndependentLayers;
import static org.dan.ping.pong.app.tournament.TournamentType.Console;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.playoff.PlayOffRule;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.console.TournamentRelationType;
import org.dan.ping.pong.app.tournament.rel.RelatedTournamentsService;

import java.util.List;

import javax.inject.Inject;

@Slf4j
public class DispatchingCategoryPlayOffBuilder implements CategoryPlayOffBuilder {
    @Inject
    private IndependentGrpLrdPlayOffBuilder indepGroupLayeredCategoryPlayOffBuilder;

    @Inject
    private MergedGrpLrdPlayOffBuilder mergedGrpLrdPlayOffBuilder;

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
                    forGroupConsole(sCid, bids);
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

    public void forGroupConsole(SelectedCid sCid, List<ParticipantMemState> bids) {
        switch (sCid.rules().getPlayOff().flatMap(PlayOffRule::getLayerPolicy)
                .orElse(IndependentLayers)) {
            case IndependentLayers:
                indepGroupLayeredCategoryPlayOffBuilder.buildIndependent(sCid, bids);
                break;
            case MergeLayers:
                mergedGrpLrdPlayOffBuilder.buildMerged(sCid, bids);
                break;
            default:
                throw internalError("Not implemented for "
                        + sCid.rules().getPlayOff()
                        .flatMap(PlayOffRule::getLayerPolicy)
                        .orElse(IndependentLayers));
        }
    }
}
