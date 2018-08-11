package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.match.MatchTag.consoleTagO;

import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class IndependentGrpLrdPlayOffRebuilder extends IndependentGrpLrdPlayOffAbstract {
    @Inject
    private CastingLotsService castingLotsService;

    @Inject
    private FlatCategoryPlayOffBuilder flatCategoryPlayOffBuilder;

    @Override
    protected void buildLayer(
            SelectedCid sCid,
            Map<Integer, List<ParticipantMemState>> bidsByFinalGroupPosition,
            int iLevel) {
        flatCategoryPlayOffBuilder.rebuild(
                sCid, bidsByFinalGroupPosition.get(iLevel),
                consoleTagO(iLevel));
    }
}
