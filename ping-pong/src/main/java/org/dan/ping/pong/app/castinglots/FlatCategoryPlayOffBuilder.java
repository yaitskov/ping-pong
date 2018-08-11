package org.dan.ping.pong.app.castinglots;

import static java.util.Collections.singleton;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.playoff.PlayOffService.roundPlayOffBase;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.MatchTag;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class FlatCategoryPlayOffBuilder implements CategoryPlayOffBuilder {
    @Inject
    private CastingLotsService castingLotsService;

    @Inject
    private ParticipantRankingService rankingService;

    @Override
    public void build(SelectedCid sCid, List<ParticipantMemState> bids) {
        final PlayOffGenerator generator = castingLotsService
                .createPlayOffGen(sCid, empty(), 0, Gold);
        buildRanked(bids, empty(), 0, generator);
    }

    public Optional<Mid> buildRanked(List<ParticipantMemState> bids,
            Optional<Mid> mergeMid, int baseLevel, PlayOffGenerator generator) {
        log.info("Flat casting in tournament {} in cid {}",
                generator.getTournament().getTid(), generator.getCid());
        validateBidsNumberInACategory(bids);
        final List<ParticipantMemState> orderedBids = rankingService.sort(bids,
                generator.getTournament().getRule().getCasting(), generator.getTournament());
        return build(orderedBids, mergeMid, baseLevel, generator);
    }

    @Inject
    private BidService bidService;

    private boolean oneParticipantInCategory(TournamentMemState tournament, Cid cid,
            List<ParticipantMemState> bids, DbUpdater batch, Optional<MatchTag> tag) {
        if (bids.size() > 1) {
            return false;
        }
        log.info("Autocomplete category {} with tag {} in tid {} due it has 1 participant",
                cid, tag, tournament.getTid());
        bidService.setBidState(bids.get(0), Win1, singleton(bids.get(0).state()), batch);
        return true;
    }

    public Optional<Mid> build(List<ParticipantMemState> orderedBids,
            Optional<Mid> mergeMid, int baseLevel,
            PlayOffGenerator generator) {
        validateBidsNumberInACategory(orderedBids);
        if (oneParticipantInCategory(generator.getTournament(), generator.getCid(),
                orderedBids, generator.getBatch(), generator.getTag())) {
            return empty();
        }
        final int roundedBasePositions = roundPlayOffBase(orderedBids.size());
        final Mid subRootMid = castingLotsService.generatePlayOffMatches(
                generator, roundedBasePositions, 1, mergeMid, baseLevel);
        assignBidsToBaseMatches(
                generator.getSCid(), roundedBasePositions,
                orderedBids, generator.getTag());
        return Optional.of(subRootMid);
    }

    public void rebuild(SelectedCid sCid,
            List<ParticipantMemState> orderedBids, Optional<MatchTag> matchTag) {
        validateBidsNumberInACategory(orderedBids);
        if (oneParticipantInCategory(sCid.tournament(), sCid.cid(),
                orderedBids, sCid.batch(), matchTag)) {
            return;
        }
        final int roundedBasePositions = roundPlayOffBase(orderedBids.size());
        assignBidsToBaseMatches(sCid, roundedBasePositions, orderedBids, matchTag);
    }

    @Inject
    private MatchService matchService;

    @Inject
    private PlayOffService playOffService;

    @Inject
    private AssignBidsToBaseMatches assignBidsToBaseMatches;

    public static void validateBidsNumberInACategory(List<ParticipantMemState> bids) {
        if (bids.size() > 128) {
            throw badRequest("PlayOff has more than 128 participants");
        }
    }

    protected void assignBidsToBaseMatches(SelectedCid sCid, int basePositions,
            List<ParticipantMemState> orderedBids, Optional<MatchTag> tag) {
        final List<MatchInfo> baseMatches = playOffService
                .findBaseMatches(sCid.tournament(), sCid.cid(), tag)
                .stream()
                .sorted(Comparator.comparing(MatchInfo::getMid))
                .collect(toList());

        assignBidsToBaseMatches.seedBaseMatches(
                basePositions, baseMatches, orderedBids, sCid);
    }
}
