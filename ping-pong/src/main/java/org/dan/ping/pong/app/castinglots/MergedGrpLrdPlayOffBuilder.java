package org.dan.ping.pong.app.castinglots;

import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.castinglots.FlatCategoryPlayOffBuilder.validateBidsNumberInACategory;
import static org.dan.ping.pong.app.match.MatchTag.consoleTagO;
import static org.dan.ping.pong.app.match.MatchTag.mergeTagO;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.match.MatchType.POff;
import static org.dan.ping.pong.app.playoff.PlayOffService.findLevels;
import static org.dan.ping.pong.app.playoff.PlayOffService.roundPlayOffBase;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.rel.RelatedTournamentsService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

public class MergedGrpLrdPlayOffBuilder extends GroupLayeredCategoryPlayOffBuilder {
    @Inject
    private FlatCategoryPlayOffBuilder flatCategoryPlayOffBuilder;

    @Inject
    private MatchService matchService;

    @Inject
    private RelatedTournamentsService relatedTournaments;

    @Inject
    private CastingLotsService castingLotsService;

    void buildLayer(List<ParticipantMemState> bids, Mid rootMid,
            Optional<Integer> levelO, SelectedCid sCid) {
        switch (bids.size()) {
            case 0:
                matchService.assignBidToMatch(sCid.tournament(), rootMid,
                        FILLER_LOSER_BID, sCid.batch());
                break;
            case 1:
                matchService.assignBidToMatch(sCid.tournament(), rootMid,
                        bids.get(0).getBid(), sCid.batch());
                break;
            default:
                flatCategoryPlayOffBuilder.build(bids, Optional.of(rootMid), 0,
                        castingLotsService
                                .createPlayOffGen(
                                        sCid,
                                        consoleTagO(
                                                levelO.orElseThrow(() -> internalError(
                                                        "no level " + sCid.tid()))),
                                        0, POff));
        }
    }

    public void buildMerged(SelectedCid sCid, List<ParticipantMemState> bids) {
        validateBidsNumberInACategory(bids);
        final TournamentMemState masterTournament = relatedTournaments.findParent(sCid.tid());

        final Map<Integer, List<ParticipantMemState>> bidsByFinalGroupPosition
                = findFinalPositions(sCid.tournament(), masterTournament, bids);

        final GeneratingConsoleLayersForBaseMatches mergingLadderGen
                = new GeneratingConsoleLayersForBaseMatches(this, bidsByFinalGroupPosition);

        final int alignedBasePositions = roundPlayOffBase(bidsByFinalGroupPosition.size());
        final int baseLevel = bidsByFinalGroupPosition.values().stream()
                .mapToInt(b -> findLevels(roundPlayOffBase(b.size())))
                .max().getAsInt();

        final PlayOffGenerator generator = castingLotsService
                .createPlayOffGen(sCid, mergeTagO(0), baseLevel, Gold);

        castingLotsService.generatePlayOffMatches(
                generator, alignedBasePositions, 1, empty(), baseLevel);

        final List<MatchInfo> baseMergeMatches = findMatchesByLevel(
                baseLevel + 1, sCid.tournament().findMatchesByCid(sCid.cid()));

        mergingLadderGen.seedBaseMatches(alignedBasePositions, baseMergeMatches,
                bidsByFinalGroupPosition.keySet().stream().sorted().collect(toList()),
                sCid);
    }

    private List<MatchInfo> findMatchesByLevel(int level, Stream<MatchInfo> s) {
        return s.filter(m -> m.getLevel() == level)
                .sorted(comparing(MatchInfo::getMid))
                .collect(toList());
    }
}
