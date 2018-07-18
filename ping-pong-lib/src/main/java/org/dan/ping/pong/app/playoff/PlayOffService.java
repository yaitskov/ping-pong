package org.dan.ping.pong.app.playoff;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.ConsoleLayered;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.CumDiffBalls;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.CumDiffSets;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.Level;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostMatches;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofIntD;
import static org.dan.ping.pong.app.match.rule.reason.IncreasingIntScalarReason.ofIntI;
import static org.dan.ping.pong.app.playoff.PlayOffBidStat.PLAY_OFF_BID_STAT_COMPARATOR;
import static org.dan.ping.pong.app.tournament.GroupMaxMap.findMaxes;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConOff;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.CategoryService;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchTag;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.service.common.BallsBalanceRuleService;
import org.dan.ping.pong.app.sport.MatchRules;
import org.dan.ping.pong.app.sport.Sport;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TidRelation;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentResultEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class PlayOffService {
    public List<MatchInfo> findBaseMatches(TournamentMemState tournament,
            Cid cid, Optional<MatchTag> tag) {
        return findBaseMatches(findPlayOffMatches(tournament, cid, tag));
    }

    public List<MatchInfo> findBaseMatches(List<MatchInfo> cidMatches) {
        Map<Mid, Mid> midChild = new HashMap<>();
        cidMatches.forEach(m -> {
            m.getWinnerMid().ifPresent(wmid -> midChild.put(wmid, m.getMid()));
            m.getLoserMid().ifPresent(wmid -> midChild.put(wmid, m.getMid()));
        });
        return cidMatches.stream()
                .filter(m -> !m.getGid().isPresent())
                .filter(m -> !midChild.containsKey(m.getMid()))
                .collect(toList());
    }

    public List<MatchInfo> findPlayOffMatches(
            TournamentMemState tournament, Cid cid, Optional<MatchTag> tag) {
        return tournament.getMatches().values().stream()
                .filter(matchInfo -> tag.map(t -> tag.equals(matchInfo.getTag())).orElse(true))
                .filter(minfo -> minfo.getCid().equals(cid))
                .filter(minfo -> !minfo.getGid().isPresent())
                .sorted(Comparator.comparing(MatchInfo::getMid))
                .collect(toList());
    }

    @Inject
    private GroupService groupService;

    @Inject
    private CategoryService categoryService;

    @Inject
    private BallsBalanceRuleService ballsBalanceRuleService;

    public PlayOffResultEntries playOffResult(TournamentMemState tournament, Cid cid,
            List<TournamentResultEntry> groupOrdered) {
        final Map<Bid, PlayOffBidStat> uidStat = new HashMap<>();
        final MatchRules matchRules = tournament.playOffMatchRules();
        final Sport sport = sports.get(matchRules.sport());
        final PowerRange powerRange = tournament.getPowerRange();
        final int maxSetDiff = sport.maxUpLimitSetsDiff(matchRules);
        final int maxBallsDiff = sport.maxUpLimitBallsDiff(matchRules);
        categoryService.findMatchesInCategoryStream(tournament, cid)
                .filter(m -> !m.getGid().isPresent())
                .forEach(m -> handlePlayOffMatch(
                        m, uidStat, powerRange, maxSetDiff,
                        maxBallsDiff, tournament));


        if (uidStat.isEmpty()) {
            return tournamentOfSingle(tournament, cid);
        }
        uidStat.remove(FILLER_LOSER_BID);
        return PlayOffResultEntries.builder()
                .entries(
                        uidStat.values().stream()
                                .sorted(new PlayOffBidComparator(
                                        PLAY_OFF_BID_STAT_COMPARATOR, groupOrdered))
                                .map(stat -> statToResultEntry(stat, tournament))
                                .collect(toList()))
                .playOffBids(uidStat.keySet())
                .build();
    }

    private void handlePlayOffMatch(
            MatchInfo m, Map<Bid, PlayOffBidStat> uidStat, PowerRange powerRange,
            int maxSetDiff, int maxBallsDiff, TournamentMemState tournament) {
        if (m.bids().size() < 2) {
            m.bids().forEach(uid ->
                    uidStat.compute(uid, (u, stat) -> {
                        if (stat == null) {
                            stat = new PlayOffBidStat(u);
                        }
                        stat.maxLevel(m.getLevel());
                        return stat;
                    }));
            return;
        }
        final Bid[] bids = m.bidsArray();
        final Map<Bid, Integer> ballsBalance = ballsBalanceRuleService
                .uid2BallsBalance(Stream.of(m));
        final Map<Bid, Integer> sets = sports.calcWonSets(tournament, m);
        final int setBalance = sets.get(bids[0]) - sets.get(bids[1]);
        final long setPower = powerRange.value(maxSetDiff, m.getLevel());
        final long ballPower = powerRange.value(maxBallsDiff, m.getLevel());
        uidStat.compute(bids[0], (u, stat) -> {
            if (stat == null) {
                stat = new PlayOffBidStat(u);
            }
            stat.addSetsBalance(setBalance * setPower);
            stat.addBallsBalance(ballsBalance.getOrDefault(u, 0) * ballPower);
            stat.maxLevel(m.getLevel());
            if (m.getWinnerId().isPresent() && !m.getWinnerId().get().equals(u)) {
                stat.incLost();
            }
            return stat;
        });
        uidStat.compute(bids[1], (u, stat) -> {
            if (stat == null) {
                stat = new PlayOffBidStat(u);
            }
            stat.addSetsBalance(-setBalance * setPower);
            stat.addBallsBalance(ballsBalance.getOrDefault(u, 0) * ballPower);
            stat.maxLevel(m.getLevel());
            if (m.getWinnerId().isPresent() && !m.getWinnerId().get().equals(u)) {
                stat.incLost();
            }
            return stat;
        });
    }

    private PlayOffResultEntries tournamentOfSingle(TournamentMemState tournament, Cid cid) {
        final List<TournamentResultEntry> entries = tournament.getParticipants()
                .values()
                .stream()
                .filter(bid -> bid.state() == Win1 && bid.getCid().equals(cid))
                .map(bid -> TournamentResultEntry.builder()
                        .user(bid.toBidLink())
                        .playOffStep(Optional.of(1))
                        .state(Win1)
                        .reasonChain(Stream
                                .<Reason>of(
                                        ofIntI(1, Level),
                                        ofIntI(0, LostMatches),
                                        ofIntD(0, CumDiffSets),
                                        ofIntD(0, CumDiffBalls))
                                .map(Optional::of)
                                .collect(toList()))
                        .build())
                .collect(toList());
        return PlayOffResultEntries
                .builder()
                .playOffBids(entries.stream()
                        .map(e -> e.getUser().getBid())
                        .collect(Collectors.toSet()))
                .entries(entries)
                .build();
    }

    private TournamentResultEntry statToResultEntry(PlayOffBidStat stat,
            TournamentMemState tournament) {
        final ParticipantMemState participant = tournament.getParticipant(
                stat.getBid());
        return TournamentResultEntry.builder()
                .user(participant.toBidLink())
                .playOffStep(Optional.of(stat.getHighestLevel()))
                .state(participant.state())
                .reasonChain(stat.toReasonChain())
                .build();
    }

    @Inject
    private Sports sports;

    private Map<MatchTag, MatchInfo> findRootMatches(TournamentMemState tournament) {
        if (tournament.getRule().getCasting().getSplitPolicy() == ConsoleLayered) {
            return findMaxes(m -> m.getTag().get(),
                    Comparator.comparing(MatchInfo::getLevel),
                    tournament.getMatches().values().stream()
                            .filter(matchInfo -> matchInfo.getTag().isPresent()));
        }
        return emptyMap();
    }

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> tournamentRelationCache;

    @SneakyThrows
    public PlayOffMatches playOffMatches(TournamentMemState tournament,
            Cid cid, Optional<MatchTag> tag) {
        final List<MatchLink> transitions = new ArrayList<>();
        final Map<Bid, String> participants = new HashMap<>();
        final List<PlayOffMatch> matches = findPlayOffMatches(tournament, cid, tag)
                .stream()
                .filter(m -> !m.isLosersMeet())
                .map(m -> this.toPlayOffMatch(tournament, m, transitions, participants))
                .collect(toList());

        final RelatedTids relatedTids = tournamentRelationCache.get(tournament.getTid());

        return PlayOffMatches.builder()
                .transitions(transitions)
                .matches(matches)
                .masterTid(relatedTids.parentTid())
                .consoleTid(ofNullable(relatedTids.getChildren().get(ConOff)))
                .rootTaggedMatches(findRootMatches(tournament)
                        .values().stream()
                        .map(MatchInfo::toRootTaggedMatch)
                        .collect(toList()))
                .participants(participants)
                .build();
    }

    private PlayOffMatch toPlayOffMatch(
            TournamentMemState tournament, MatchInfo m,
            List<MatchLink> transitions, Map<Bid, String> participants) {
        m.getWinnerMid().ifPresent(wMid -> transitions.add(
                MatchLink.builder()
                        .from(m.getMid())
                        .to(wMid)
                        .build()));
        if (!m.hasParticipant(FILLER_LOSER_BID)) {
            m.getLoserMid().ifPresent(lMid -> transitions.add(
                    MatchLink.builder()
                            .from(m.getMid())
                            .to(lMid)
                            .build()));
        }
        m.getParticipantIdScore()
                .keySet()
                .stream()
                .filter(bid -> !FILLER_LOSER_BID.equals(bid))
                .forEach(bid -> participants.computeIfAbsent(bid,
                        (u -> tournament.getParticipant(u).getName())));

        final Map<Bid, Integer> score = sports.calcWonSets(tournament, m);
        return PlayOffMatch.builder()
                .id(m.getMid())
                .level(m.getLevel())
                .score(score)
                .walkOver(isWalkOver(tournament, m, score))
                .state(m.getState())
                .winnerId(m.getWinnerId())
                .build();
    }

    private boolean isWalkOver(TournamentMemState tournament, MatchInfo m,
            Map<Bid, Integer> score) {
        if (m.getState() != Over) {
            return false;
        }
        final Optional<Bid> calculatedWinner = sports.findWinnerId(
                tournament.selectMatchRule(m), score);
        return !calculatedWinner.equals(m.getWinnerId());
    }

    public Stream<MatchInfo> findMatchesByLevelAndCid(
            int level, Cid cid, Stream<MatchInfo> stream) {
        return stream.filter(m -> m.getCid().equals(cid) && m.getLevel() == level);
    }
}
