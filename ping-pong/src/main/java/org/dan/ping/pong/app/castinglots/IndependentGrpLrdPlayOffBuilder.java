package org.dan.ping.pong.app.castinglots;

import static java.util.Optional.empty;
import static org.dan.ping.pong.app.match.MatchTag.consoleTagO;
import static org.dan.ping.pong.app.match.MatchType.Gold;

import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class IndependentGrpLrdPlayOffBuilder extends IndependentGrpLrdPlayOffAbstract {
    @Inject
    private CastingLotsService castingLotsService;

    @Inject
    private FlatCategoryPlayOffBuilder flatCategoryPlayOffBuilder;

    @Override
    protected void buildLayer(
            SelectedCid sCid,
            Map<Integer, List<ParticipantMemState>> bidsByFinalGroupPosition,
            int iLevel) {
        final PlayOffGenerator generator = castingLotsService
                .createPlayOffGen(sCid, consoleTagO(iLevel), 0, Gold);

        flatCategoryPlayOffBuilder.build(
                bidsByFinalGroupPosition.get(iLevel), empty(), 0, generator);
    }
}