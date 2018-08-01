package org.dan.ping.pong.app.castinglots;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.castinglots.PlayOffLoserSelector.selectLosersForConsole;
import static org.dan.ping.pong.app.match.MatchTag.consoleTagO;
import static org.dan.ping.pong.app.match.MatchTag.mergeTagO;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.match.MatchType.POff;
import static org.dan.ping.pong.app.playoff.PlayOffService.roundAndFindLevels;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.util.collection.MapUtils.makeKeysSequential;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.rel.RelatedTournamentsService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class PlayOffLayeredCategoryPlayOffBuilder {
    @Inject
    private FlatCategoryPlayOffBuilder ladderBuilder;

    @Inject
    private RelatedTournamentsService relatedTournaments;

    @Inject
    private PlayOffService playOffService;

    @Inject
    private MatchService matchService;

    @Inject
    private CastingLotsService castingLotsService;

    @Inject
    private BidService bidService;

    public void buildMerged(SelectedCid sCid) {
        final Map<Integer, List<Bid>> bidsByLevels = compact(findBidLevels(sCid));
        final int maxLevel = bidsByLevels.keySet().stream().mapToInt(o -> o)
                .max().orElseThrow(() -> internalError("no play off matches"));
        int finalLevel = roundAndFindLevels(bidsByLevels.get(1).size())
                + maxLevel - 1;
        Optional<Mid> lastMergeMid = empty();
        for (int iLevel = maxLevel; iLevel > 1; --iLevel) {
            lastMergeMid = Optional.of(matchService.createPlayOffMatch(
                        sCid, lastMergeMid, empty(), 0, finalLevel,
                        iLevel == maxLevel ? Gold : POff,
                        mergeTagO(finalLevel)));
            attachLevel(sCid, bidsByLevels, --finalLevel, lastMergeMid, iLevel);
        }
        attachLevel(sCid, bidsByLevels, finalLevel, lastMergeMid, 1);
    }

    public void buildIndependent(SelectedCid sCid) {
        final Map<Integer, List<Bid>> bidsByLevels = compact(findBidLevels(sCid));
        bidsByLevels.forEach((iLevel, bids) -> {
            final List<ParticipantMemState> levelBids = consoleParticipantsOnLevel(
                    sCid, bidsByLevels, iLevel);
            if (levelBids.size() == 1) {
                bidService.setBidState(levelBids.get(0), Win1, sCid.batch());
            } else if (levelBids.size() > 1) {
                buildForManyBids(sCid, 1, Optional.empty(), iLevel, levelBids);
            } else {
                throw internalError("empty level " + iLevel
                        + " in " + sCid.tid() + " " + sCid.cid());
            }
        });
    }

    private Map<Integer, List<Bid>> compact(Map<Integer, List<Bid>> bidsByLevels) {
        final List<Integer> emptyLevels = bidsByLevels.entrySet().stream()
                .filter(e -> e.getValue().isEmpty())
                .map(Map.Entry::getKey).collect(toList());
        emptyLevels.forEach(bidsByLevels::remove);
        return makeKeysSequential(bidsByLevels);
    }

    public void attachLevel(SelectedCid sCid, Map<Integer, List<Bid>> bidsByLevels,
            int finalLevel, Optional<Mid> lastMergeMid, int iLevel) {
        final List<ParticipantMemState> levelBids = consoleParticipantsOnLevel(
                sCid, bidsByLevels, iLevel);
        if (levelBids.size() == 1) {
            matchService.assignBidToMatch(sCid.tournament(), lastMergeMid.get(),
                    levelBids.get(0).getBid(), sCid.batch());
        } else if (levelBids.size() > 1) {
            buildForManyBids(sCid, finalLevel, lastMergeMid, iLevel, levelBids);
        } else {
            throw internalError("empty level " + iLevel
                    + " in " + sCid.tid() + " " + sCid.cid());
        }
    }

    public List<ParticipantMemState> consoleParticipantsOnLevel(
            SelectedCid sCid, Map<Integer, List<Bid>> bidsByLevels, int iLevel) {
        return ofNullable(bidsByLevels.get(iLevel))
                .map(lBids -> lBids.stream().map(sCid::getBid).collect(toList()))
                .orElseThrow(() -> internalError("no bids on level "
                        + iLevel + " in tid " + sCid.tid() + " cid " + sCid.cid()));
    }

    private void buildForManyBids(
            SelectedCid sCid, int finalLevel, Optional<Mid> lastMergeMid,
            int iLevel, List<ParticipantMemState> levelBids) {
        final PlayOffGenerator generator = castingLotsService.createPlayOffGen(
                sCid, consoleTagO(iLevel), finalLevel - 1,
                lastMergeMid.map(m -> POff).orElse(Gold));
        ladderBuilder.buildRanked(
                levelBids, lastMergeMid, finalLevel - 1, generator);
    }

    public Map<Integer, List<Bid>> findBidLevels(SelectedCid sCid) {
        final TournamentMemState mTour = relatedTournaments.findParent(sCid.tid());

        final List<MatchInfo> playOffMatches = playOffService
                .findPlayOffMatches(
                        mTour, mTour.findCidByName(sCid.category()), empty());

        return selectLosersForConsole(mTour, playOffMatches.stream());
    }
}
