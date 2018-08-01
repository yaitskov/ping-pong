package org.dan.ping.pong.app.castinglots;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.app.match.MatchType.Brnz;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.util.collection.FilterCollector.filtering;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchType;
import org.dan.ping.pong.app.playoff.PlayOffGuests;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.util.collection.MaxValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayOffLoserSelector {
    private static final Map<MatchType, BidState> WIN_MATCH = ImmutableMap.of(
            Gold, Win1,
            Brnz, Win3);

    public static Map<Integer, List<Bid>> selectLosersForConsole(
            TournamentMemState mTour, Stream<MatchInfo> matches) {
        final PlayOffGuests guests = mTour.playOffRules().consoleParticipants();
        final MaxValue<Integer> maxLevel = new MaxValue<>(1);
        final Map<BidState, Bid> winState2Bid = new HashMap<>(2);

        final Map<Integer, List<Bid>> result = matches
                .filter(MatchInfo::isOver)
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

    private static Map<Integer, List<Bid>> winningGuests(
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
