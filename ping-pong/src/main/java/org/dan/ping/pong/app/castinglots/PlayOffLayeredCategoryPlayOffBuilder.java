package org.dan.ping.pong.app.castinglots;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.match.MatchTag.consoleTagO;
import static org.dan.ping.pong.app.match.MatchTag.mergeTagO;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.match.MatchType.POff;
import static org.dan.ping.pong.app.playoff.PlayOffService.roundAndFindLevels;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.util.collection.FilterCollector.filtering;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.playoff.PlayOffGuests;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.rel.RelatedTournamentsService;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class PlayOffLayeredCategoryPlayOffBuilder implements CategoryPlayOffBuilder {
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

    @Override
    public void build(TournamentMemState conTour, Cid coCid,
            List<ParticipantMemState> bids, DbUpdater batch) {
        final Map<Integer, List<Bid>> bidsByLevels = findBidLevels(conTour, coCid);
        final List<Integer> emptyLevels = bidsByLevels.entrySet().stream()
                .filter(e -> e.getValue().isEmpty())
                .map(Map.Entry::getKey).collect(toList());
        emptyLevels.forEach(bidsByLevels::remove);
        final int maxLevel = bidsByLevels.keySet().stream().mapToInt(o -> o)
                .max().orElseThrow(() -> internalError("no play off matches"));
        int finalLevel = roundAndFindLevels(bidsByLevels.get(1).size())
                + maxLevel - 1;
        Optional<Mid> lastMergeMid = empty();
        for (int iLevel = maxLevel; iLevel > 1; --iLevel) {
            lastMergeMid = Optional.of(matchService.createPlayOffMatch(
                        conTour, coCid, lastMergeMid, empty(), 0, finalLevel,
                        iLevel == maxLevel ? Gold : POff,
                        mergeTagO(finalLevel), batch));
            attachLevel(conTour, coCid, batch, bidsByLevels,
                    --finalLevel, lastMergeMid, iLevel);
        }
        attachLevel(conTour, coCid, batch, bidsByLevels,
                finalLevel, lastMergeMid, 1);
    }

    public void attachLevel(TournamentMemState conTour, Cid cid, DbUpdater batch,
            Map<Integer, List<Bid>> bidsByLevels,
            int finalLevel, Optional<Mid> lastMergeMid, int iLevel) {
        final List<ParticipantMemState> levelBids = ofNullable(bidsByLevels.get(iLevel))
                .map(lBids -> lBids.stream().map(conTour::getBid).collect(toList()))
                .orElseThrow(() -> internalError("no bids on level "
                        + iLevel + " in tid " + conTour.getTid() + " cid " + cid));
        if (levelBids.size() == 1) {
            matchService.assignBidToMatch(
                    conTour, lastMergeMid.get(), levelBids.get(0).getBid(), batch);
        } else if (levelBids.size() > 1) {
            final PlayOffGenerator generator = castingLotsService.createPlayOffGen(
                    batch, conTour, cid, consoleTagO(iLevel), finalLevel - 1,
                    lastMergeMid.map(m -> POff).orElse(Gold));
            ladderBuilder.buildRanked(
                    levelBids, lastMergeMid, finalLevel - 1, generator);
        } else {
            throw internalError("empty level " + iLevel
                    + " in " + conTour.getTid() + " " + cid);
        }
    }

    private Map<Integer, List<Bid>> findBidLevels(TournamentMemState conTour, Cid cid) {
        final TournamentMemState mTour = relatedTournaments
                .findParent(conTour.getTid());

        final List<MatchInfo> playOffMatches = playOffService
                .findPlayOffMatches(
                        mTour,
                        mTour.findCidByName(conTour.getCategory(cid)),
                        Optional.empty());
        final PlayOffGuests guests = mTour.playOffRules().consoleParticipants();

        return playOffMatches.stream().collect(groupingBy(
                MatchInfo::getLevel,
                filtering(m -> !m.getLoserMid().isPresent()
                                && m.getOpponentBid()
                                .map(mTour::getBidOrQuit)
                                .map(bid -> guests.bidStateForConsole(
                                        bid.getBidState(), m.getType()))
                                .orElse(false),
                        mapping(MatchInfo::getOpponentBid,
                                filtering(Optional::isPresent,
                                        mapping(Optional::get,
                                                toList()))))));
    }
}
