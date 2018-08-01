package org.dan.ping.pong.app.castinglots;

import static java.util.Optional.empty;
import static org.dan.ping.pong.app.castinglots.FlatCategoryPlayOffBuilder.validateBidsNumberInACategory;
import static org.dan.ping.pong.app.match.MatchTag.consoleTagO;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.rel.RelatedTournamentsService;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class IndependentGrpLrdPlayOffBuilder extends GroupLayeredCategoryPlayOffBuilder {
    @Inject
    private FlatCategoryPlayOffBuilder flatCategoryPlayOffBuilder;

    @Inject
    private RelatedTournamentsService relatedTournaments;

    @Inject
    private CastingLotsService castingLotsService;

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
            final PlayOffGenerator generator = castingLotsService
                    .createPlayOffGen(sCid, consoleTagO(iLevel), 0, Gold);

            flatCategoryPlayOffBuilder.build(
                    bidsByFinalGroupPosition.get(iLevel), empty(), 0, generator);
        }
    }
}