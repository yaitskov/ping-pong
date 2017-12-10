package org.dan.ping.pong.app.group;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.group.ParticipantMatchState.Pending;
import static org.dan.ping.pong.app.group.ParticipantMatchState.Run;
import static org.dan.ping.pong.app.group.ParticipantMatchState.WALKOVER;
import static org.dan.ping.pong.app.group.ParticipantMatchState.WALKWINER;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        return orderUidsInGroup(tournament, uid -> Play, groupMatches)
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
            Function<Uid, BidState> participantStateF,
            List<MatchInfo> allMatchesInGroup) {
        final Map<Uid, BidSuccessInGroup> uid2Stat = emptyMatchesState(participantStateF, allMatchesInGroup);
        final MatchValidationRule matchRule = tournament.getRule().getMatch();
        allMatchesInGroup.forEach(minfo -> aggMatch(uid2Stat, minfo, matchRule));
        return order(uid2Stat.values(), tournament.getRule().getGroup().get().getDisambiguation())
                .stream().map(BidSuccessInGroup::getUid)
                .collect(toList());
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
                    return WALKWINER;
                } else {
                    return WALKOVER;
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
