package org.dan.ping.pong.app.castinglots;

import static java.util.Collections.singleton;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.castinglots.PlayOffGenerator.PLAY_OFF_SEEDS;
import static org.dan.ping.pong.app.match.MatchService.roundPlayOffBase;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.MatchTag;
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
    private CastingLotsDao castingLotsDao;

    @Inject
    private ParticipantRankingService rankingService;

    @Override
    public void build(TournamentMemState tournament, Cid cid,
            List<ParticipantMemState> bids, DbUpdater batch) {
        log.info("Flat casting in tournament {} in cid {}", tournament.getTid(), cid);
        validateBidsNumberInACategory(bids);
        final List<ParticipantMemState> orderedBids = rankingService.sort(bids,
                tournament.getRule().getCasting(), tournament);
        build(tournament, cid, orderedBids, batch, Optional.empty());
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

    public void build(TournamentMemState tournament, Cid cid,
            List<ParticipantMemState> orderedBids, DbUpdater batch, Optional<MatchTag> tag) {
        validateBidsNumberInACategory(orderedBids);
        if (oneParticipantInCategory(tournament, cid, orderedBids, batch, tag)) {
            return;
        }
        final int basePositions = roundPlayOffBase(orderedBids.size());
        castingLotsDao.generatePlayOffMatches(batch, tournament, cid, basePositions, 1, tag);
        assignBidsToBaseMatches(cid, basePositions, orderedBids, tournament, batch, tag);
    }

    @Inject
    private MatchService matchService;

    @Inject
    private PlayOffService playOffService;

    public static void validateBidsNumberInACategory(List<ParticipantMemState> bids) {
        if (bids.size() > 128) {
            throw badRequest("Category has more than 128 participants");
        }
    }

    protected void assignBidsToBaseMatches(Cid cid, int basePositions,
            List<ParticipantMemState> orderedBids,
            TournamentMemState tournament, DbUpdater batch, Optional<MatchTag> tag) {
        final List<Integer> seeds = ofNullable(PLAY_OFF_SEEDS.get(basePositions))
                .orElseThrow(() -> internalError("No seeding for "
                        + orderedBids.size() + " participants"));

        final List<MatchInfo> baseMatches = playOffService
                .findBaseMatches(tournament, cid, tag)
                .stream()
                .sorted(Comparator.comparing(MatchInfo::getMid))
                .collect(toList());

        for (int iMatch = 0; iMatch < basePositions / 2; ++iMatch) {
            final MatchInfo match = baseMatches.get(iMatch);
            final int iBid1 = seeds.get(iMatch * 2);
            final int iBid2 = seeds.get(iMatch * 2 + 1);
            final int iStrongBid = Math.min(iBid1, iBid2);
            final int iWeakBid = Math.max(iBid1, iBid2);

            final ParticipantMemState strongBid = orderedBids.get(iStrongBid);
            matchService.assignBidToMatch(tournament, match.getMid(),
                    strongBid.getBid(), batch);

            if (iWeakBid >= orderedBids.size()) {
                matchService.assignBidToMatch(tournament, match.getMid(), FILLER_LOSER_BID, batch);
            } else {
                final ParticipantMemState weakBid = orderedBids.get(iWeakBid);
                matchService.assignBidToMatch(tournament, match.getMid(), weakBid.getBid(), batch);
            }
        }
    }
}
