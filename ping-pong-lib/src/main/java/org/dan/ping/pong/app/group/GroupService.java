package org.dan.ping.pong.app.group;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.group.ParticipantMatchState.Pending;
import static org.dan.ping.pong.app.group.ParticipantMatchState.Run;
import static org.dan.ping.pong.app.group.ParticipantMatchState.WalkOver;
import static org.dan.ping.pong.app.group.ParticipantMatchState.WalkWiner;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.UseDisambiguationMatches;
import static org.dan.ping.pong.app.match.rule.service.GroupRuleParams.ofParams;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.CastingLotsDaoIf;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchParticipants;
import org.dan.ping.pong.app.match.MatchTag;
import org.dan.ping.pong.app.match.NoDisambiguateMatchesException;
import org.dan.ping.pong.app.match.rule.GroupParticipantOrder;
import org.dan.ping.pong.app.match.rule.GroupParticipantOrderService;
import org.dan.ping.pong.app.match.rule.GroupPosition;
import org.dan.ping.pong.app.match.rule.rules.meta.PreviewDisambiguationDirective;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentResultEntry;
import org.dan.ping.pong.app.tournament.TournamentRules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;


@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupService {
    public static final Optional<MatchTag> MATCH_TAG_DISAMBIGUATION = Optional.of(
            MatchTag.builder().prefix(MatchTag.DISAMBIGUATION).build());

    public Optional<List<MatchInfo>> checkGroupComplete(
            TournamentMemState tournament, int gid) {
        final List<MatchInfo> matches = findAllMatchesInGroup(tournament, gid);
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        final long completedMatches = matches.stream()
                .map(MatchInfo::getState)
                .filter(Over::equals)
                .count();

        if (completedMatches < matches.size()) {
            log.debug("Matches {} left to play in the group {}",
                    matches.size() - completedMatches, gid);
            return Optional.empty();
        }
        return Optional.of(matches);
    }

    public Map<Integer, List<MatchInfo>> groupMatchesByGroup(TournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getGid().isPresent())
                .collect(groupingBy(mInfo -> mInfo.getGid().get()));
    }

    public List<MatchInfo> findAllMatchesInGroup(TournamentMemState tournament, int gid) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getGid().equals(Optional.of(gid)))
                .collect(toList());
    }

    @Inject
    private GroupParticipantOrderService groupParticipantOrderService;

    public List<Uid> orderUidsInGroup(int gid, TournamentMemState tournament,
            List<MatchInfo> groupMatches) {
        final GroupParticipantOrder orderedUids = groupParticipantOrderService
                .findOrder(ofParams(gid, tournament, groupMatches, tournament.orderRules(),
                        tournament.uidsInGroup(gid)));
        if (orderedUids.unambiguous()) {
            return orderedUids.determinedUids();
        }
        throw new NoDisambiguateMatchesException(orderedUids);
    }

    @Inject
    private Sports sports;

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
        int[] c = new int[1];
        return tournament.getMatches()
                .values()
                .stream()
                .filter(m -> ogid.equals(m.getGid()))
                .peek(m -> ++c[0])
                .anyMatch(m -> m.getState() != Over) || c[0] == 0;
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
        final TournamentRules rules = tournament.getRule();

        final List<MatchInfo> groupMatches = findAllMatchesInGroup(tournament, gid);
        final GroupParticipantOrder order = groupParticipantOrderService.findOrder(
                ofParams(gid, tournament, groupMatches, tournament.orderRules(),
                        tournament.uidsInGroup(gid)));

        final List<ParticipantMemState> seedBidsOrder = rankingService
                .sort(groupBids(tournament, gid), rules.getCasting(), tournament);
        final List<Uid> finalPositions = order.getPositions()
                .values()
                .stream()
                .map(e -> e.getReason().get().getUid())
                .collect(toList());
        final Map<Uid, GroupParticipantResult> result = order.getPositions()
                .values()
                .stream()
                .collect(toMap(
                        e -> e.getReason().get().getUid(),
                        e -> groupPosToResult(tournament, e,
                                groupMatches.stream()
                                        .filter(m -> m.hasParticipant(
                                                e.getReason().get().getUid())))));

        range(0, finalPositions.size()).forEach(
                i -> result.get(finalPositions.get(i)).setFinishPosition(i));
        range(0,  finalPositions.size()).forEach(
                i -> result.get(seedBidsOrder.get(i).getUid()).setSeedPosition(i));

        final GroupRules groupRules = tournament.getRule().getGroup().get();

        return GroupParticipants.builder()
                .tid(tournament.getTid())
                .participants(result.values())
                .quitsGroup(groupRules.getQuits())
                .build();
    }

    private GroupParticipantResult groupPosToResult(
            TournamentMemState tournament, GroupPosition gp, Stream<MatchInfo> matches) {
        final ParticipantMemState bid = tournament.getParticipant(
                gp.getReason().get().getUid());
        return GroupParticipantResult.builder()
                .name(bid.getName())
                .uid(bid.getUid())
                .state(bid.getState())
                .reasonChain(gp.reasonChain())
                .matches(matches
                        .collect(toMap(
                                m -> m.getOpponentUid(bid.getUid()).get(),
                                m -> matchResult(bid.getUid(), tournament, m))))
                .build();
    }

    private List<ParticipantMemState> groupBids(TournamentMemState tournament, int gid) {
        return tournament.getParticipants().values().stream()
                .filter(bid -> bid.getGid().equals(of(gid))).collect(toList());
    }

    private GroupMatchResult matchResult(Uid uid, TournamentMemState tournament, MatchInfo m) {
        final Uid oUid = m.getOpponentUid(uid)
                .orElseThrow(() -> internalError("no opponent uid" + uid));
        final Map<Uid, Integer> uid2WonSets = sports.calcWonSets(tournament, m);
        final Optional<Uid> scoreWinner = sports.findWinnerId(
                tournament.selectMatchRule(m), uid2WonSets);
        return GroupMatchResult.builder()
                .state(participantMatchState(uid, scoreWinner, m))
                .mid(m.getMid())
                .sets(HisIntPair.builder()
                        .his(uid2WonSets.get(uid))
                        .enemy(uid2WonSets.get(oUid))
                        .build())
                .games(pairGames(uid, oUid, m.getParticipantIdScore()))
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

    public List<TournamentResultEntry> resultOfAllGroupsInCategory(
            TournamentMemState tournament, int cid) {
        final List<MatchInfo> allGroupMatches = tournament.findGroupMatchesByCategory(cid);
        if (allGroupMatches.isEmpty()) {
            return tournamentOfSingle(tournament, cid);
        }
        final GroupParticipantOrder order = groupParticipantOrderService.findOrder(
                ofParams(0, tournament, allGroupMatches,
                        tournament.orderRules().stream()
                                .map(r -> r.name() == UseDisambiguationMatches
                                        ? new PreviewDisambiguationDirective()
                                        : r)
                                .collect(toList()),
                        tournament.uidsInCategory(cid)));

        return order.getPositions().values()
                .stream()
                .map(gp -> {
                    final Uid uid = gp.getReason().get().getUid();
                    final ParticipantMemState participant = tournament.getParticipant(uid);
                    return TournamentResultEntry.builder()
                            .user(participant.toLink())
                            .playOffStep(empty())
                            .state(participant.getState())
                            .reasonChain(gp.reasonChain())
                            .build();
                })
                .collect(toList());
    }

    private List<TournamentResultEntry> tournamentOfSingle(TournamentMemState tournament, int cid) {
        return tournament.getParticipants().values().stream()
                .filter(bid -> bid.getState() == Win1 && bid.getCid() == cid)
                .map(bid -> TournamentResultEntry.builder()
                        .user(bid.toLink())
                        .playOffStep(Optional.empty())
                        .state(Win1)
                        .reasonChain(emptyList())
                        .build())
                .collect(toList());
    }

    public boolean notExpelledInGroup(TournamentMemState tournament, ParticipantMemState b) {
        return b.getState() != Expl || tournament.participantMatches(b.getUid())
                .anyMatch(m -> !m.getGid().isPresent());
    }

    public void ensureThatNewGroupCouldBeAdded(TournamentMemState tournament, int cid) {
        final List<GroupInfo> categoryGroups = tournament.getGroupsByCategory(cid);
        categoryGroups.forEach(groupInfo -> {
            if (!isNotCompleteGroup(tournament, groupInfo.getGid())) {
                throw badRequest("category-has-complete-group", "link", groupInfo.toLink());
            }
        });
    }

    public static String sortToLabel(int sort) {
        return "Group " + (1 + sort);
    }

    @Inject
    private GroupDao groupDao;

    public int createGroup(TournamentMemState tournament, int cid) {
        final int sort = tournament.getGroups().values().stream()
                .map(GroupInfo::getOrdNumber)
                .max(Integer::compare).orElse(-1) + 1;
        final String label = sortToLabel(sort);
        final int gid = groupDao.createGroup(tournament.getTid(), cid, label,
                tournament.getRule().getGroup().get().getQuits(), sort);
        log.info("New group {}/{} is created in tid/cid {}/{}",
                gid, label, tournament.getTid(), cid);
        tournament.getGroups().put(gid, GroupInfo.builder().gid(gid).cid(cid)
                .ordNumber(sort).label(label).build());
        return gid;
    }

    public Set<Integer> findIncompleteGroups(TournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(mInfo -> mInfo.getGid().isPresent() && mInfo.getState() != Over)
                .collect(toMap(mInfo -> mInfo.getGid().get(),
                        o -> o, (a, b) -> a))
                .keySet();
    }

    @Inject
    private CastingLotsDaoIf castingLotsDao;

    public void createDisambiguateMatches(
            NoDisambiguateMatchesException e,
            TournamentMemState tournament,
            List<MatchInfo> allGroupMatches) {
        if (allGroupMatches.stream().anyMatch(m -> m.getTag().isPresent())) {
            throw internalError("Attempt to create multiple series of disambiguation matches");
        }
        final int gid = allGroupMatches.get(0).getGid().get();

        for (GroupPosition group : e.getUids().ambiguousGroups()) {
            createDisambiguateMatches(tournament, gid, group.getCompetingUids());
        }
    }

    public void createDisambiguateMatches(TournamentMemState tournament,
            int gid, Collection<Uid> uids) {
        castingLotsDao.generateGroupMatches(tournament, gid,
                uids.stream()
                        .map(tournament::getParticipant)
                        .collect(toList()), 0,
                MATCH_TAG_DISAMBIGUATION);
    }

    public void createDisambiguateMatches(TournamentMemState tournament,
            int gid, MatchParticipants mp) {
        createDisambiguateMatches(tournament, gid,
                asList(mp.getUidLess(), mp.getUidMore()));
    }
}
