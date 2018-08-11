package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.castinglots.FlatCategoryPlayOffBuilder.validateBidsNumberInACategory;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.rel.RelatedTournamentsService;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public abstract class IndependentGrpLrdPlayOffAbstract
        extends GroupLayeredCategoryPlayOffBuilder {
    @Inject
    private RelatedTournamentsService relatedTournaments;

    protected abstract void buildLayer(
            SelectedCid sCid,
            Map<Integer, List<ParticipantMemState>> bidsByFinalGroupPosition,
            int iLevel);

    public void buildIndependent(SelectedCid sCid, List<ParticipantMemState> bids) {
        validateBidsNumberInACategory(bids);
        final TournamentMemState masterTournament = relatedTournaments.findParent(sCid.tid());

        final Map<Integer, List<ParticipantMemState>> bidsByFinalGroupPosition
                = findFinalPositions(sCid.tournament(), masterTournament, bids);

        final int limit = bidsByFinalGroupPosition.keySet().stream()
                .mapToInt(o -> o + 1).max()
                .orElseThrow(() -> internalError("No Levels in groups of cid "
                        + sCid.cid() + " tid " + sCid.tid()));
        for (int iLevel = masterTournament.groupRules().getQuits();
             iLevel < limit; ++iLevel) {
            buildLayer(sCid, bidsByFinalGroupPosition, iLevel);
        }
    }
}
