package org.dan.ping.pong.app.castinglots;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.util.collection.MapUtils.makeKeysSequential;
import static org.dan.ping.pong.app.match.MatchTag.consoleTagO;
import static org.dan.ping.pong.app.match.MatchTag.mergeTagO;
import static org.dan.ping.pong.app.match.MatchType.Brnz;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.match.MatchType.POff;
import static org.dan.ping.pong.app.playoff.PlayOffService.roundAndFindLevels;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.util.collection.FilterCollector.filtering;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.MatchType;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.playoff.PlayOffGuests;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.rel.RelatedTournamentsService;
import org.dan.ping.pong.util.collection.MaxValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private static final Map<MatchType, BidState> WIN_MATCH = ImmutableMap.of(
            Gold, Win1,
            Brnz, Win3);

    private Map<Integer, List<Bid>> findBidLevels(SelectedCid sCid) {
        final TournamentMemState mTour = relatedTournaments.findParent(sCid.tid());

        final List<MatchInfo> playOffMatches = playOffService
                .findPlayOffMatches(
                        mTour, mTour.findCidByName(sCid.category()), empty());
        final PlayOffGuests guests = mTour.playOffRules().consoleParticipants();
        final MaxValue<Integer> maxLevel = new MaxValue<>(1);
        final Map<BidState, Bid> winState2Bid = new HashMap<>(2);
        final Map<Integer, List<Bid>> result = playOffMatches
                .stream()
                .peek(m -> maxLevel.accept(m.getLevel()))
                .peek(m ->
                        ofNullable(WIN_MATCH.get(m.getType())).ifPresent(
                                bidState -> winState2Bid.put(bidState, m.winnerId())))
                .collect(groupingBy(
                        MatchInfo::getLevel,
                        filtering(m -> !m.getLoserMid().isPresent()
                                        && m.loserBid()
                                        .map(mTour::getBidOrQuit)
                                        .map(bid -> guests.bidStateForConsole(
                                                bid.getBidState(), m.getType()))
                                        .orElse(false),
                                mapping(MatchInfo::loserBid,
                                        filtering(Optional::isPresent,
                                                mapping(Optional::get,
                                                        Collectors.<Bid>toList()))))));

        return winningGuests(winState2Bid, maxLevel.getMax(), result, mTour);
    }

    private Map<Integer, List<Bid>> winningGuests(
            Map<BidState, Bid> winState2Bid, Integer max,
            Map<Integer, List<Bid>> bidsByLevels, TournamentMemState tournament) {
        switch (tournament.playOffRules().consoleParticipants()) {
            case AndWinner1:
                ofNullable(winState2Bid.get(Win1)).ifPresent(w1Bid ->
                        bidsByLevels.put(max + 2, singletonList(w1Bid)));
                ofNullable(winState2Bid.get(Win3)).ifPresent(w3Bid -> {
                    List<Bid> win2 = bidsByLevels.get(max);
                    bidsByLevels.put(max + 1, win2);
                    bidsByLevels.put(max, singletonList(w3Bid));
                });
                break;
            case AndWinner2:
                ofNullable(winState2Bid.get(Win3)).ifPresent(w3Bid -> {
                    List<Bid> win2 = bidsByLevels.get(max);
                    bidsByLevels.put(max + 1, win2);
                    bidsByLevels.put(max, singletonList(w3Bid));
                });
                break;
            case AndWinner3:
                ofNullable(winState2Bid.get(Win3)).ifPresent(w3Bid ->
                        bidsByLevels.put(max + 1, singletonList(w3Bid)));
                break;
            case JustLosers:
            case LosersUpToSemifinals:
                break; // ok
            default:
                throw internalError("not implemented for "
                        + tournament.playOffRules().consoleParticipants());
        }
        return bidsByLevels;
    }
}
