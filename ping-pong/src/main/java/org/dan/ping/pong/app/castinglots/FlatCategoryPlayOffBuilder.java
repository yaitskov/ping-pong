package org.dan.ping.pong.app.castinglots;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.castinglots.PlayOffGenerator.PLAY_OFF_SEEDS;
import static org.dan.ping.pong.app.match.MatchService.roundPlayOffBase;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_UID;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.MatchTag;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

@Slf4j
public class FlatCategoryPlayOffBuilder implements CategoryPlayOffBuilder {
    @Inject
    private CastingLotsDao castingLotsDao;

    @Inject
    private ParticipantRankingService rankingService;

    @Override
    public void build(TournamentMemState tournament, Integer cid,
            List<ParticipantMemState> bids, DbUpdater batch) {
        log.info("Flat casting in tournament {} in cid {}", tournament.getTid(), cid);
        validateBidsNumberInACategory(bids);
        final List<ParticipantMemState> orderedBids = rankingService.sort(bids,
                tournament.getRule().getCasting(), tournament);
        build(tournament, cid, orderedBids, batch, null);
    }

    public void build(TournamentMemState tournament, Integer cid,
            List<ParticipantMemState> orderedBids, DbUpdater batch, MatchTag tag) {
        validateBidsNumberInACategory(orderedBids);
        final int basePositions = roundPlayOffBase(orderedBids.size());
        castingLotsDao.generatePlayOffMatches(tournament, cid, basePositions, 1, tag);
        assignBidsToBaseMatches(cid, basePositions, orderedBids, tournament, batch, tag);
    }

    @Inject
    private MatchService matchService;

    @Inject
    private PlayOffService playOffService;

    public static void validateBidsNumberInACategory(List<ParticipantMemState> bids) {
        if (bids.size() < 2) {
            throw badRequest("There is a category with 1 participant."
                    + " Expel him/her or move into another category.");
        }
        if (bids.size() > 128) {
            throw badRequest("Category has more than 128 participants");
        }
    }

    protected void assignBidsToBaseMatches(Integer cid, int basePositions,
            List<ParticipantMemState> orderedBids,
            TournamentMemState tournament, DbUpdater batch, MatchTag tag) {
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
                    strongBid.getUid(), batch);

            if (iWeakBid >= orderedBids.size()) {
                matchService.assignBidToMatch(tournament, match.getMid(), FILLER_LOSER_UID, batch);
            } else {
                final ParticipantMemState weakBid = orderedBids.get(iWeakBid);
                matchService.assignBidToMatch(tournament, match.getMid(), weakBid.getUid(), batch);
            }
        }
    }
}
