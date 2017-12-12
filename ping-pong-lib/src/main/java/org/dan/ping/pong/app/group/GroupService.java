package org.dan.ping.pong.app.group;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.group.ParticipantMatchState.Pending;
import static org.dan.ping.pong.app.group.ParticipantMatchState.Run;
import static org.dan.ping.pong.app.group.ParticipantMatchState.WalkOver;
import static org.dan.ping.pong.app.group.ParticipantMatchState.WalkWiner;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchValidationRule;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentRules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;


@Slf4j
public class GroupService {
    public Map<Uid, BidSuccessInGroup> emptyMatchesState(
            Function<Uid, BidState> participantState,
            Collection<MatchInfo> allMatchesInGroup) {
        return allMatchesInGroup.stream()
                .map(MatchInfo::getParticipantIdScore)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .collect(toMap(uid -> uid,
                        uid -> new BidSuccessInGroup(uid, participantState.apply(uid)),
                        (a, b) -> a));
    }

    public List<Uid> findUidsQuittingGroup(TournamentMemState tournament,
            GroupRules groupRules, List<MatchInfo> groupMatches) {
        // todo rule option skip walk over or not
        return orderUidsInGroup(tournament, groupMatches)
                .stream()
                .limit(groupRules.getQuits())
                .collect(toList());
    }

    public List<MatchInfo> findMatchesInGroup(TournamentMemState tournament, int gid) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getGid().equals(Optional.of(gid)))
                .collect(toList());
    }

    public List<Uid> orderUidsInGroup(TournamentMemState tournament,
            List<MatchInfo> allMatchesInGroup) {
        final Map<Uid, Integer> uid2Points = countPoints(allMatchesInGroup);
        final SetMultimap<Uid, Uid> strongerExtraOrder = findStrongerExtraOrder(tournament,
                uid2Points, allMatchesInGroup);

        final List<Uid> result = uid2Points.keySet().stream()
                .sorted(UidGroupComparator.builder()
                        .uid2Points(uid2Points)
                        .strongerExtraOrder(strongerExtraOrder)
                        .build())
                .collect(toList());
        log.info("Final uids order in group: {}", result);
        return result;
    }

    SetMultimap<Uid, Uid> findStrongerExtraOrder(TournamentMemState tournament,
            Map<Uid, Integer> uid2Points, List<MatchInfo> matches) {
        final SetMultimap<Integer, Uid> ambiguousUids = findParticipantsWithSamePoints(uid2Points);

        final SetMultimap<Uid, Uid> strongerExtraOrder = HashMultimap.create();

        for (Integer points : ambiguousUids.keySet()) {
            final Set<Uid> uids =  ambiguousUids.get(points);
            final int numUids = uids.size();
            if (numUids == 2) {
                Iterator<Uid> uidIterator = uids.iterator();
                compareDirectMatch(matches, uidIterator.next(),
                        uidIterator.next(), strongerExtraOrder);
            } else if (numUids > 2) {
                compareMatchesBetweenMany(tournament, matches, uids, strongerExtraOrder);
            } else {
                throw internalError("wrong number of uids " + numUids);
            }
        }
        return strongerExtraOrder;
    }

    private void orderUidsRandomly(int gid, Set<Uid> uids, SetMultimap<Uid, Uid> strongerExtraOrder) {
        final List<Uid> orderedUids = new ArrayList<>(uids);
        Collections.sort(orderedUids);
        Collections.shuffle(orderedUids, new Random(gid));
        orderListToStrongerMultimap(strongerExtraOrder, orderedUids);
    }

    private void compareMatchesBetweenMany(TournamentMemState tournament,
            List<MatchInfo> matches, Set<Uid> uids,
            SetMultimap<Uid, Uid> strongerExtraOrder) {

        final List<MatchInfo> matchesWithUids = filterMatchesByUids(matches, uids);

        final Map<Uid, BidSuccessInGroup> uid2Stat = emptyMatchesState(uid -> Play, matchesWithUids);
        final MatchValidationRule matchRule = tournament.getRule().getMatch();
        matchesWithUids.forEach(minfo -> aggMatch(uid2Stat, minfo, matchRule));

        orderTwiceAmbiguous(tournament, matches, uids,
                strongerExtraOrder, matchesWithUids, uid2Stat);
        orderJustAmbiguous(tournament, strongerExtraOrder, uid2Stat);
    }

    private void orderJustAmbiguous(TournamentMemState tournament,
            SetMultimap<Uid, Uid> strongerExtraOrder,
            Map<Uid, BidSuccessInGroup> uid2Stat) {
        final Comparator<BidSuccessInGroup> comparator = tournament.getRule().getGroup()
                .get().getDisambiguation().getComparator();
        final List<BidSuccessInGroup> orderded = uid2Stat.values().stream()
                .sorted(comparator)
                .collect(toList());
        for (int i = 0; i < orderded.size() - 1; ++i) {
            final BidSuccessInGroup iStat = orderded.get(i);
            for (int j = i + 1; j < orderded.size(); ++j) {
                final BidSuccessInGroup jStat = orderded.get(j);
                if (comparator.compare(iStat, jStat) < 0) {
                    strongerExtraOrder.put(iStat.getUid(), jStat.getUid());
                }
            }
        }
    }

    private void orderTwiceAmbiguous(TournamentMemState tournament,
            List<MatchInfo> matches, Set<Uid> uids,
            SetMultimap<Uid, Uid> strongerExtraOrder,
            List<MatchInfo> matchesWithUids,
            Map<Uid, BidSuccessInGroup> uid2Stat) {
        final Map<Uid, PointSetBallComparableWrapper> uid2StatWrapped = uid2Stat.values().stream()
                .collect(toMap(
                        BidSuccessInGroup::getUid,
                        stat -> PointSetBallComparableWrapper.builder()
                                .stat(stat)
                                .build()));

        final SetMultimap<PointSetBallComparableWrapper, Uid> ambiguousUids
                = findParticipantsWithSamePoints(uid2StatWrapped);

        for (PointSetBallComparableWrapper stat : ambiguousUids.keySet()) {
            final Set<Uid> subUids = ambiguousUids.get(stat);
            final int numUids = subUids.size();
            if (numUids == 2) {
                Iterator<Uid> uidIterator = uids.iterator();
                compareDirectMatch(matches, uidIterator.next(),
                        uidIterator.next(), strongerExtraOrder);
            } else if (numUids > 2) {
                if (numUids == uids.size()) { // all uids
                    orderUidsRandomly(matches.get(0).getGid()
                                    .orElseThrow(() -> internalError("no gid in match "
                                            + matches.get(0).getMid())),
                            uids, strongerExtraOrder);
                } else {
                    compareMatchesBetweenMany(tournament, matchesWithUids,
                            subUids, strongerExtraOrder);
                }
            } else {
                throw internalError("wrong number of uids " + numUids);
            }
        }
    }

    private List<MatchInfo> filterMatchesByUids(List<MatchInfo> matches, Set<Uid> uids) {
        return matches.stream()
                .filter(m -> !m.getParticipantIdScore().isEmpty()
                        && m.getParticipantIdScore().keySet().stream()
                        .allMatch(uids::contains))
                .collect(toList());
    }

    private void orderListToStrongerMultimap(SetMultimap<Uid, Uid> strongerExtraOrder, List<Uid> orderedUids) {
        log.info("Ordered uids {}", orderedUids);
        for (int i = 0; i < orderedUids.size() - 1; ++i) {
            for (int j = i + 1; j < orderedUids.size(); ++j) {
                strongerExtraOrder.put(orderedUids.get(i), orderedUids.get(j));
            }
        }
    }

    private void compareDirectMatch(List<MatchInfo> matches, Uid uidA, Uid uidB,
            SetMultimap<Uid, Uid> strongerExtraOrder) {
        final MatchInfo abMatch = matches.stream()
                .filter(m -> m.hasParticipant(uidA) && m.hasParticipant(uidB))
                .findAny().orElseThrow(() -> internalError(
                        "no match between " + uidA + " and " + uidB));
        final Uid winnerUid = abMatch.getWinnerId()
                .orElseThrow(() -> internalError("no winner in match " + abMatch.getMid()));
        strongerExtraOrder.put(winnerUid, abMatch.getOpponentUid(winnerUid)
                .orElseThrow(() -> internalError("no opponent in match " + abMatch.getMid())));
    }

    private Map<Uid, Integer> countPoints(List<MatchInfo> allMatchesInGroup) {
        CounterMap<Uid> counters = new CounterMap<>();
        allMatchesInGroup.stream().map(MatchInfo::getWinnerId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(counters::increment);
        return counters.toMap();
    }

    private <T> SetMultimap<T, Uid> findParticipantsWithSamePoints(Map<Uid, T> uid2Points) {
        final Map<T, Uid> metPoints = new HashMap<>();
        final SetMultimap<T, Uid> result = HashMultimap.create();
        uid2Points.forEach((uid, points) -> {
            final Uid firstUid = metPoints.putIfAbsent(points, uid);
            if (firstUid != null) {
                if (result.get(points).isEmpty()) {
                    result.put(points, firstUid);
                }
                result.put(points, uid);
            }
        });
        return result;
    }

    public Collection<BidSuccessInGroup> order(
            Collection<BidSuccessInGroup> bidSuccess,
            DisambiguationPolicy disambiguation) {
        return bidSuccess.stream()
                .sorted(disambiguation.getComparator())
                .collect(toList());
    }

    public void aggMatch(Map<Uid, BidSuccessInGroup> uid2Stat,
            MatchInfo minfo, MatchValidationRule matchRule) {
        if (!minfo.getWinnerId().isPresent()) {
            log.error("Match {} is not complete", minfo.getMid());
            return;
        }
        final Uid winUid = minfo.getWinnerId().get();
        final BidSuccessInGroup winner = uid2Stat.get(winUid);
        final Uid lostUid = minfo.getOpponentUid(winUid).get();
        final BidSuccessInGroup loser = uid2Stat.get(lostUid);

        minfo.getParticipantScore(winUid)
                .forEach(winner::winBalls);
        minfo.getParticipantScore(winUid)
                .forEach(loser::lostBalls);
        minfo.getParticipantScore(lostUid)
                .forEach(loser::winBalls);
        minfo.getParticipantScore(lostUid)
                .forEach(winner::lostBalls);

        final Map<Uid, Integer> uid2Sets = matchRule.calcWonSets(minfo.getParticipantIdScore());

        loser.wonSets(uid2Sets.get(lostUid));
        loser.lostSets(uid2Sets.get(winUid));
        winner.wonSets(uid2Sets.get(winUid));
        winner.lostSets(uid2Sets.get(lostUid));

        winner.win();
        matchRule.findWinnerId(uid2Sets)
                .ifPresent(uid -> loser.lost()); // walkover = 0
    }

    public GroupPopulations populations(TournamentMemState tournament, int cid) {
        final List<GroupLink> groupLinks = tournament.getGroupsByCategory(cid).stream()
                .sorted(Comparator.comparingInt(GroupInfo::getOrdNumber))
                .map(GroupInfo::toLink)
                .collect(toList());
        final Map<Integer, Long> gidNumMatches = tournament.getParticipants()
                .values().stream()
                .filter(p -> p.getCid() == cid)
                .filter(p -> p.getGid().isPresent())
                .collect(Collectors.groupingBy(p -> p.getGid().get(),
                        Collectors.counting()));
        return GroupPopulations.builder()
                .links(groupLinks)
                .populations(groupLinks.stream()
                        .map(g -> gidNumMatches.getOrDefault(g.getGid(), 0L))
                        .collect(toList()))
                .build();
    }

    public boolean isNotCompleteGroup(TournamentMemState tournament, int gid) {
        final Optional<Integer> ogid = Optional.of(gid);
        return tournament.getMatches()
                .values()
                .stream()
                .anyMatch(m -> ogid.equals(m.getGid())
                        && m.getState() != Over);
    }

    public GroupWithMembers members(TournamentMemState tournament, int gid) {
        final GroupInfo groupInfo = ofNullable(tournament.getGroups().get(gid))
                .orElseThrow(() -> notFound("group not found", "gid", gid));
        return GroupWithMembers.builder()
                .gid(gid)
                .name(groupInfo.getLabel())
                .category(tournament.getCategory(groupInfo.getCid()))
                .members(tournament.getParticipants().values().stream()
                        .filter(p -> p.getGid().equals(of(gid)))
                        .map(ParticipantMemState::toLink)
                        .collect(toList()))
                .build();
    }

    @Inject
    private ParticipantRankingService rankingService;

    public GroupParticipants result(TournamentMemState tournament, int gid) {
        final List<MatchInfo> matches = findMatchesInGroup(tournament, gid);
        final List<ParticipantMemState> bids = groupBids(tournament, gid);
        final TournamentRules rules = tournament.getRule();
        final Map<Uid, BidSuccessInGroup> uid2Stat = emptyMatchesState(
                uid -> Play,
                matches);
        final MatchValidationRule matchRule = tournament.getRule().getMatch();
        matches.forEach(minfo -> aggMatch(uid2Stat, minfo, matchRule));
        final DisambiguationPolicy disambiguation = tournament.getRule()
                .getGroup().get().getDisambiguation();
        final List<Uid> finalUidsOrder = order(uid2Stat.values(), disambiguation)
                .stream().map(BidSuccessInGroup::getUid)
                .collect(toList());

        final List<ParticipantMemState> seedBidsOrder = rankingService
                .sort(bids, rules.getCasting());
        final Map<Uid, GroupParticipantResult> result = bids.stream().collect(toMap(
                ParticipantMemState::getUid,
                bid -> {
                    final Optional<BidSuccessInGroup> bidResult = ofNullable(uid2Stat.get(bid.getUid()));
                    return GroupParticipantResult.builder()
                            .name(bid.getName())
                            .uid(bid.getUid())
                            .punkts(bidResult.get().getPunkts())
                            .matches(matches.stream()
                                    .filter(m -> m.hasParticipant(bid.getUid()))
                                    .collect(toMap(
                                            m -> m.getOpponentUid(bid.getUid()).get(),
                                            m -> matchResult(bid.getUid(), tournament, m))))
                            .build();
                }));

        range(0, finalUidsOrder.size()).forEach(
                i -> result.get(finalUidsOrder.get(i)).setFinishPosition(i));
        range(0, finalUidsOrder.size()).forEach(
                i -> result.get(seedBidsOrder.get(i).getUid()).setSeedPosition(i));

        return GroupParticipants.builder()
                .tid(tournament.getTid())
                .participants(result.values())
                .build();
    }

    private List<ParticipantMemState> groupBids(TournamentMemState tournament, int gid) {
        return tournament.getParticipants().values().stream()
                .filter(bid -> bid.getGid().equals(of(gid))).collect(toList());
    }

    private GroupMatchResult matchResult(Uid uid, TournamentMemState tournament, MatchInfo m) {
        final Uid oUid = m.getOpponentUid(uid)
                .orElseThrow(() -> internalError("no opponent uid" + uid));
        final MatchValidationRule matchRule = tournament.getRule().getMatch();
        final Map<Uid, List<Integer>> scores = m.getParticipantIdScore();
        final Map<Uid, Integer> uid2WonSets = matchRule.calcWonSets(scores);
        final Optional<Uid> scoreWinner = matchRule.findWinnerId(uid2WonSets);
        return GroupMatchResult.builder()
                .state(participantMatchState(uid, scoreWinner, m))
                .mid(m.getMid())
                .sets(HisIntPair.builder()
                        .his(uid2WonSets.get(uid))
                        .enemy(uid2WonSets.get(oUid))
                        .build())
                .games(pairGames(uid, oUid, scores))
                .build();
    }

    private ParticipantMatchState participantMatchState(
            Uid uid, Optional<Uid> scoreWinner, MatchInfo m) {
        if (m.getWinnerId().isPresent()) {
            if (scoreWinner.isPresent()) {
                if (scoreWinner.equals(m.getWinnerId())) {
                    return ParticipantMatchState.Over;
                }
                throw internalError("match is over by score but winner does not mismatch");
            } else {
                if (m.getWinnerId().get().equals(uid)) {
                    return WalkWiner;
                } else {
                    return WalkOver;
                }
            }
        } else {
            switch (m.getState()) {
                case Draft:
                case Place:
                    return Pending;
                case Game:
                case Auto:
                    return Run;
                default:
                    throw internalError("bad state " + m.getState());
            }
        }
    }

    private List<HisIntPair> pairGames(Uid uid, Uid oUid, Map<Uid, List<Integer>> scores) {
        final List<Integer> setsA = scores.get(uid);
        final List<Integer> setsB = scores.get(oUid);
        final List<HisIntPair> result = new ArrayList<>();
        for (int i = 0; i < setsA.size(); ++i) {
            result.add(HisIntPair.builder()
                    .his(setsA.get(i))
                    .enemy(setsB.get(i))
                    .build());
        }
        return result;
    }
}
