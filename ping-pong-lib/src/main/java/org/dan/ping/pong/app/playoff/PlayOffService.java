package org.dan.ping.pong.app.playoff;

import static java.util.Collections.emptyMap;
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
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_UID;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.CategoryService;
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

@Slf4j
public class PlayOffService {
    public List<MatchInfo> findBaseMatches(TournamentMemState tournament,
            int cid, Optional<MatchTag> tag) {
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
            TournamentMemState tournament, int cid, Optional<MatchTag> tag) {
        return tournament.getMatches().values().stream()
                .filter(matchInfo -> tag.map(t -> tag.equals(matchInfo.getTag())).orElse(true))
                .filter(minfo -> minfo.getCid() == cid)
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

    public PlayOffResultEntries playOffResult(TournamentMemState tournament, int cid,
            List<TournamentResultEntry> groupOrdered) {
        final Map<Uid, PlayOffBidStat> uidStat = new HashMap<>();
        final MatchRules matchRules = tournament.playOffMatchRules();
        final Sport sport = sports.get(matchRules.sport());
        final PowerRange powerRange = tournament.getPowerRange();
        final int maxSetDiff = sport.maxUpLimitSetsDiff(matchRules);
        final int maxBallsDiff = sport.maxUpLimitBallsDiff(matchRules);
        categoryService.findMatchesInCategoryStream(tournament, cid)
                .filter(m -> !m.getGid().isPresent())
                .forEach(m -> {
                    if (m.uids().size() < 2) {
                        m.uids().forEach(uid -> {
                            uidStat.compute(uid, (u, stat) -> {
                                if (stat == null) {
                                    stat = new PlayOffBidStat(u);
                                }
                                stat.maxLevel(m.getLevel());
                                return stat;
                            });
                        });
                        return;
                    }
                    final Uid[] uids = m.uidsArray();
                    final Map<Uid, Integer> ballsBalance = ballsBalanceRuleService
                            .uid2BallsBalance(Stream.of(m));
                    final Map<Uid, Integer> sets = sports.calcWonSets(tournament, m);
                    final int setBalance = sets.get(uids[0]) - sets.get(uids[1]);
                    final long setPower = powerRange.value(maxSetDiff, m.getLevel());
                    final long ballPower = powerRange.value(maxBallsDiff, m.getLevel());
                    uidStat.compute(uids[0], (u, stat) -> {
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
                    uidStat.compute(uids[1], (u, stat) -> {
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
                });

        if (uidStat.isEmpty()) {
            return tournamentOfSingle(tournament, cid);
        }
        uidStat.remove(FILLER_LOSER_UID);
        return PlayOffResultEntries.builder()
                .entries(
                        uidStat.values().stream()
                                .sorted(new PlayOffBidComparator(PLAY_OFF_BID_STAT_COMPARATOR, groupOrdered))
                                .map(stat -> statToResultEntry(stat, tournament))
                                .collect(toList()))
                .playOffUids(uidStat.keySet())
                .build();
    }

    private PlayOffResultEntries tournamentOfSingle(TournamentMemState tournament, int cid) {
        final List<TournamentResultEntry> entries = tournament.getParticipants()
                .values()
                .stream()
                .filter(bid -> bid.state() == Win1 && bid.getCid() == cid)
                .map(bid -> TournamentResultEntry.builder()
                        .user(bid.toLink())
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
                .playOffUids(entries.stream()
                        .map(e -> e.getUser().getUid())
                        .collect(Collectors.toSet()))
                .entries(entries)
                .build();
    }

    private TournamentResultEntry statToResultEntry(PlayOffBidStat stat,
            TournamentMemState tournament) {
        final ParticipantMemState participant = tournament.getParticipant(
                stat.getUid());
        return TournamentResultEntry.builder()
                .user(participant.toLink())
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

    public PlayOffMatches playOffMatches(TournamentMemState tournament,
            int cid, Optional<MatchTag> tag) {
        final List<MatchLink> transitions = new ArrayList<>();
        final List<PlayOffMatch> matches = new ArrayList<>();
        final Map<Uid, String> participants = new HashMap<>();

        findPlayOffMatches(tournament, cid, tag)
                .stream()
                .filter(m -> !m.isLosersMeet())
                .forEach(m -> {
                    m.getWinnerMid().ifPresent(wMid -> transitions.add(
                            MatchLink.builder()
                                    .from(m.getMid())
                                    .to(wMid)
                                    .build()));
                    if (!m.hasParticipant(FILLER_LOSER_UID)) {
                        m.getLoserMid().ifPresent(lMid -> transitions.add(
                                MatchLink.builder()
                                        .from(m.getMid())
                                        .to(lMid)
                                        .build()));
                    }
                    m.getParticipantIdScore()
                            .keySet()
                            .stream()
                            .filter(uid -> !FILLER_LOSER_UID.equals(uid))
                            .forEach(uid -> participants.computeIfAbsent(uid,
                                    (u -> tournament.getParticipant(u).getName())));

                    final Map<Uid, Integer> score = sports.calcWonSets(tournament, m);
                    matches.add(PlayOffMatch.builder()
                            .id(m.getMid())
                            .level(m.getLevel())
                            .score(score)
                            .walkOver(isWalkOver(tournament, m, score))
                            .state(m.getState())
                            .winnerId(m.getWinnerId())
                            .build());
                });

        return PlayOffMatches.builder()
                .transitions(transitions)
                .matches(matches)
                .rootTaggedMatches(findRootMatches(tournament)
                        .values().stream()
                        .map(MatchInfo::toRootTaggedMatch)
                        .collect(toList()))
                .participants(participants)
                .build();
    }

    private boolean isWalkOver(TournamentMemState tournament, MatchInfo m,
            Map<Uid, Integer> score) {
        if (m.getState() != Over) {
            return false;
        }
        final Optional<Uid> calculatedWinner = sports.findWinnerId(
                tournament.selectMatchRule(m), score);
        return !calculatedWinner.equals(m.getWinnerId());
    }

    public Stream<MatchInfo> findMatchesByLevelAndCid(
            int level, int cid, Stream<MatchInfo> stream) {
        return stream.filter(m -> m.getCid() == cid && m.getLevel() == level);
    }
}
