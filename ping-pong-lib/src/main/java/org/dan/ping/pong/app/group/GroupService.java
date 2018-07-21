package org.dan.ping.pong.app.group;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.group.GroupSchedule.DEFAULT_SCHEDULE;
import static org.dan.ping.pong.app.group.ParticipantMatchState.Pending;
import static org.dan.ping.pong.app.group.ParticipantMatchState.Run;
import static org.dan.ping.pong.app.group.ParticipantMatchState.WalkOver;
import static org.dan.ping.pong.app.group.ParticipantMatchState.WalkWiner;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.match.MatchTag.DISAMBIGUATION;
import static org.dan.ping.pong.app.match.MatchTag.ORIGIN;
import static org.dan.ping.pong.app.match.MatchType.Grup;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.UseDisambiguationMatches;
import static org.dan.ping.pong.app.match.rule.service.GroupRuleParams.ofParams;
import static org.dan.ping.pong.app.match.rule.service.meta.UseDisambiguationMatchesDirectiveService.matchesInGroup;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.CastingLotsDaoIf;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchParticipants;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.match.MatchTag;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.match.NoDisambiguateMatchesException;
import org.dan.ping.pong.app.match.rule.GroupParticipantOrder;
import org.dan.ping.pong.app.match.rule.GroupParticipantOrderService;
import org.dan.ping.pong.app.match.rule.GroupPosition;
import org.dan.ping.pong.app.match.rule.rules.meta.PreviewDisambiguationDirective;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentResultEntry;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;


@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupService {
    public static final MatchTag DM_TAG = MatchTag.builder().prefix(DISAMBIGUATION).build();
    public static final MatchTag OM_TAG = MatchTag.builder().prefix(ORIGIN).build();
    public static final Optional<MatchTag> MATCH_TAG_DISAMBIGUATION = Optional.of(DM_TAG);

    public Optional<List<MatchInfo>> checkGroupComplete(
            TournamentMemState tournament, Gid gid) {
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

    public Map<Gid, List<MatchInfo>> groupMatchesByGroup(TournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getGid().isPresent())
                .collect(groupingBy(mInfo -> mInfo.getGid().get()));
    }

    public List<MatchInfo> findAllMatchesInGroup(TournamentMemState tournament, Gid gid) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getGid().equals(Optional.of(gid)))
                .collect(toList());
    }

    @Inject
    private GroupParticipantOrderService groupParticipantOrderService;

    public List<Bid> orderBidsInGroup(Gid gid, TournamentMemState tournament,
            List<MatchInfo> groupMatches) {
        final GroupParticipantOrder orderedUids = groupParticipantOrderService
                .findOrder(ofParams(Optional.of(gid), tournament, groupMatches,
                        tournament.orderRules(), tournament.bidsInGroup(gid)));
        if (orderedUids.unambiguous()) {
            return orderedUids.determinedBids();
        }
        throw new NoDisambiguateMatchesException(orderedUids);
    }

    @Inject
    private Sports sports;

    public GroupPopulations populations(TournamentMemState tournament, Cid cid) {
        final List<GroupLink> groupLinks = tournament.getGroupsByCategory(cid).stream()
                .sorted(Comparator.comparingInt(GroupInfo::getOrdNumber))
                .map(GroupInfo::toLink)
                .collect(toList());
        final Map<Gid, Long> gidNumMatches = tournament.getParticipants()
                .values().stream()
                .filter(p -> p.getCid().equals(cid))
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

    public boolean isNotCompleteGroup(TournamentMemState tournament, Gid gid) {
        final Optional<Gid> ogid = Optional.of(gid);
        int[] c = new int[1];
        return tournament.getMatches()
                .values()
                .stream()
                .filter(m -> ogid.equals(m.getGid()))
                .peek(m -> ++c[0])
                .anyMatch(m -> m.getState() != Over) || c[0] == 0;
    }

    public GroupWithMembers members(TournamentMemState tournament, Gid gid) {
        final GroupInfo groupInfo = ofNullable(tournament.getGroups().get(gid))
                .orElseThrow(() -> notFound("group not found", "gid", gid));
        return GroupWithMembers.builder()
                .gid(gid)
                .name(groupInfo.getLabel())
                .category(tournament.getCategory(groupInfo.getCid()).toLink())
                .members(tournament.getParticipants().values().stream()
                        .filter(p -> p.getGid().equals(of(gid)))
                        .map(ParticipantMemState::toBidLink)
                        .collect(toList()))
                .build();
    }

    @Inject
    private ParticipantRankingService rankingService;

    public GroupParticipants result(TournamentMemState tournament, Gid gid) {
        final TournamentRules rules = tournament.getRule();

        final List<MatchInfo> groupMatches = findAllMatchesInGroup(tournament, gid);
        final GroupParticipantOrder order = groupParticipantOrderService.findOrder(
                ofParams(Optional.of(gid), tournament, groupMatches, tournament.orderRules(),
                        tournament.bidsInGroup(gid)));

        final List<ParticipantMemState> seedBidsOrder = rankingService
                .sort(groupBids(tournament, gid), rules.getCasting(), tournament);
        final List<Bid> finalPositions = order.getPositions()
                .values()
                .stream()
                .map(e -> e.getReason().get().getBid())
                .collect(toList());
        final boolean possibleDm = matchesInGroup(finalPositions.size()) < groupMatches.size();
        final Map<Bid, GroupParticipantResult> result = order.getPositions()
                .values()
                .stream()
                .collect(toMap(
                        e -> e.getReason().get().getBid(),
                        e -> groupPosToResult(tournament, e, possibleDm)));


        arrangeMatchesByUids(tournament, groupMatches, result);

        range(0, finalPositions.size()).forEach(
                i -> result.get(finalPositions.get(i)).setFinishPosition(i));
        range(0,  finalPositions.size()).forEach(
                i -> result.get(seedBidsOrder.get(i).getBid()).setSeedPosition(i));

        final GroupRules groupRules = tournament.getRule().getGroup().get();

        return GroupParticipants.builder()
                .tid(tournament.getTid())
                .participants(result.values())
                .quitsGroup(groupRules.getQuits())
                .sportType(tournament.getSport())
                .build();
    }

    private void arrangeMatchesByUids(TournamentMemState tournament,
            List<MatchInfo> groupMatches,
            Map<Bid, GroupParticipantResult> result) {
        groupMatches.forEach(m -> {
            if (m.getTag().isPresent()) {
                m.participants().forEach(bid -> result.get(bid)
                        .getDmMatches()
                        .put(m.opponentBid(bid), matchResult(bid, tournament, m)));
            } else {
                m.participants().forEach(bid -> result.get(bid)
                        .getOriginMatches()
                        .put(m.opponentBid(bid), matchResult(bid, tournament, m)));
            }
        });
    }

    private GroupParticipantResult groupPosToResult(
            TournamentMemState tournament, GroupPosition gp,
            boolean possibleDm) {
        final ParticipantMemState bid = tournament.getParticipant(
                gp.getReason().get().getBid());
        return GroupParticipantResult.builder()
                .name(bid.getName())
                .uid(bid.getUid())
                .bid(bid.getBid())
                .state(bid.state())
                .reasonChain(gp.reasonChain())
                .dmMatches(possibleDm ? new HashMap<>() : emptyMap())
                .originMatches(new HashMap<>())
                .build();
    }

    private List<ParticipantMemState> groupBids(TournamentMemState tournament, Gid gid) {
        return tournament.getParticipants().values().stream()
                .filter(bid -> bid.getGid().equals(of(gid))).collect(toList());
    }

    private GroupMatchResult matchResult(Bid bid, TournamentMemState tournament, MatchInfo m) {
        final Bid oBid = m.getOpponentBid(bid)
                .orElseThrow(() -> internalError("no opponent bid" + bid));
        final Map<Bid, Integer> uid2WonSets = sports.calcWonSets(tournament, m);
        final Optional<Bid> scoreWinner = sports.findWinnerId(
                tournament.selectMatchRule(m), uid2WonSets);
        return GroupMatchResult.builder()
                .state(participantMatchState(bid, scoreWinner, m))
                .mid(m.getMid())
                .sets(HisIntPair.builder()
                        .his(uid2WonSets.get(bid))
                        .enemy(uid2WonSets.get(oBid))
                        .build())
                .games(pairGames(bid, oBid, m.getParticipantIdScore()))
                .build();
    }

    private ParticipantMatchState participantMatchState(
            Bid bid, Optional<Bid> scoreWinner, MatchInfo m) {
        if (m.getWinnerId().isPresent()) {
            if (scoreWinner.isPresent()) {
                if (scoreWinner.equals(m.getWinnerId())) {
                    return ParticipantMatchState.Over;
                }
                throw internalError("match is over by score but winner does not mismatch");
            } else {
                if (m.getWinnerId().get().equals(bid)) {
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

    private List<HisIntPair> pairGames(Bid bid, Bid oBid, Map<Bid, List<Integer>> scores) {
        final List<Integer> setsA = scores.get(bid);
        final List<Integer> setsB = scores.get(oBid);
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
            TournamentMemState tournament, Cid cid) {
        final List<MatchInfo> allGroupMatches = tournament.findGroupMatchesByCategory(cid);
        if (allGroupMatches.isEmpty()) {
            return tournamentOfSingle(tournament, cid);
        }
        final GroupParticipantOrder order = groupParticipantOrderService.findOrder(
                ofParams(empty(), tournament, allGroupMatches,
                        tournament.orderRules().stream()
                                .map(r -> r.name() == UseDisambiguationMatches
                                        ? new PreviewDisambiguationDirective()
                                        : r)
                                .collect(toList()),
                        tournament.bidsInCategory(cid)));

        return order.getPositions().values()
                .stream()
                .map(gp -> {
                    final Bid bid = gp.getReason().get().getBid();
                    final ParticipantMemState participant = tournament.getParticipant(bid);
                    return TournamentResultEntry.builder()
                            .user(participant.toBidLink())
                            .playOffStep(empty())
                            .state(participant.state())
                            .reasonChain(gp.reasonChain())
                            .build();
                })
                .collect(toList());
    }

    private List<TournamentResultEntry> tournamentOfSingle(TournamentMemState tournament, Cid cid) {
        return tournament.getParticipants().values().stream()
                .filter(bid -> bid.state() == Win1 && bid.getCid().equals(cid))
                .map(bid -> TournamentResultEntry.builder()
                        .user(bid.toBidLink())
                        .playOffStep(Optional.empty())
                        .state(Win1)
                        .reasonChain(emptyList())
                        .build())
                .collect(toList());
    }

    public boolean notExpelledInGroup(TournamentMemState tournament, ParticipantMemState b) {
        return b.state() != Expl || tournament.participantMatches(b.getBid())
                .anyMatch(m -> !m.getGid().isPresent());
    }

    public void ensureThatNewGroupCouldBeAdded(TournamentMemState tournament, Cid cid) {
        final List<GroupInfo> categoryGroups = tournament.getGroupsByCategory(cid);
        categoryGroups.forEach(groupInfo -> {
            if (!isNotCompleteGroup(tournament, groupInfo.getGid())) {
                throw badRequest("category-has-complete-group", "link", groupInfo.toLink());
            }
        });
    }

    public static String sortToLabel(Gid sort) {
        return "Group " + (1 + sort.intValue());
    }

    @Inject
    private GroupDao groupDao;

    public Gid createGroup(TournamentMemState tournament, Cid cid, DbUpdater batch) {
        final Gid gid = tournament.getNextGroup().next();
        final String label = sortToLabel(gid);
        final int ordNumber = (int) tournament.findGroupsByCategory(cid).count();
        groupDao.createGroup(gid, batch,
                tournament.getTid(), cid, label,
                tournament.getRule().getGroup().get().getQuits(), ordNumber);
        log.info("New group {}/{} is created in tid/cid {}/{}",
                gid, label, tournament.getTid(), cid);
        tournament.getGroups().put(gid, GroupInfo.builder().gid(gid).cid(cid)
                .ordNumber(ordNumber).label(label).build());
        return gid;
    }

    public Set<Gid> findIncompleteGroups(TournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(mInfo -> mInfo.getGid().isPresent() && mInfo.getState() != Over)
                .collect(toMap(mInfo -> mInfo.getGid().get(),
                        o -> o, (a, b) -> a))
                .keySet();
    }

    @Inject
    private CastingLotsDaoIf castingLotsDao;

    public void createDisambiguateMatches(
            DbUpdater batch, NoDisambiguateMatchesException e,
            TournamentMemState tournament,
            List<MatchInfo> allGroupMatches) {
        if (allGroupMatches.stream().anyMatch(m -> m.getTag().isPresent())) {
            throw internalError("Attempt to create multiple series of disambiguation matches");
        }
        final Gid gid = allGroupMatches.get(0).getGid().get();

        for (GroupPosition group : e.getUids().ambiguousGroups()) {
            createDisambiguateMatches(batch, tournament, gid, group.getCompetingBids());
        }
    }

    public void createDisambiguateMatches(
            DbUpdater batch, TournamentMemState tournament,
            Gid gid, Collection<Bid> bids) {
        castingLotsDao.generateGroupMatches(batch, tournament, gid,
                bids.stream()
                        .map(tournament::getParticipant)
                        .collect(toList()), 0,
                MATCH_TAG_DISAMBIGUATION);
    }

    public void createDisambiguateMatches(DbUpdater batch, TournamentMemState tournament,
            Gid gid, MatchParticipants mp) {
        createDisambiguateMatches(batch, tournament, gid,
                asList(mp.getBidLess(), mp.getBidMore()));
    }

    public void validateMaxGroupSize(int size) {
        if (size > 30) {
            throw badRequest("Group has more than 30 participants");
        }
    }

    private List<Integer> pickSchedule(TournamentMemState tournament,
            List<ParticipantMemState> groupBids) {
        final GroupSchedule groupSchedules = tournament.getRule().getGroup().get()
                .getSchedule().orElse(DEFAULT_SCHEDULE);
        return ofNullable(groupSchedules.getSize2Schedule().get(groupBids.size()))
                .orElseGet(() -> ofNullable(DEFAULT_SCHEDULE.getSize2Schedule().get(groupBids.size()))
                        .orElseThrow(() -> internalError("No schedule for group of " + groupBids.size())));
    }

    public int generateGroupMatches(DbUpdater batch, TournamentMemState tournament, Gid gid,
            List<ParticipantMemState> groupBids, int priorityGroup,
            Optional<MatchTag> tag) {
        final Tid tid = tournament.getTid();
        log.info("Generate matches for group {} in tournament {}", gid, tid);
        final List<Integer> schedule = pickSchedule(tournament, groupBids);
        for (int i = 0; i < schedule.size();) {
            final int bidIdxA = schedule.get(i++);
            final int bidIdxB = schedule.get(i++);
            final ParticipantMemState bid1 = groupBids.get(bidIdxA);
            final ParticipantMemState bid2 = groupBids.get(bidIdxB);
            priorityGroup = addGroupMatch(batch, tournament, priorityGroup, bid1, bid2,
                    Place, Optional.empty(), tag);
        }
        return priorityGroup;
    }

    @Inject
    private MatchDao matchDao;

    public int addGroupMatch(DbUpdater batch,
            TournamentMemState tournament, int priorityGroup,
            ParticipantMemState bid1, ParticipantMemState bid2,
            MatchState state, Optional<Bid> winnerId, Optional<MatchTag> tag) {
        final Mid mid = tournament.getNextMatch().next();
        matchDao.createGroupMatch(batch, mid, bid1.getTid(),
                bid1.getGid().get(), bid1.getCid(), ++priorityGroup,
                bid1.getBid(), bid2.getBid(), tag, Place);
        tournament.getMatches().put(mid, MatchInfo.builder()
                .tid(bid1.getTid())
                .mid(mid)
                .level(0)
                .priority(priorityGroup)
                .state(state)
                .tag(tag)
                .winnerId(winnerId)
                .gid(bid1.getGid())
                .participantIdScore(ImmutableMap.of(
                        bid1.getBid(), new ArrayList<>(),
                        bid2.getBid(), new ArrayList<>()))
                .type(Grup)
                .cid(bid1.getCid())
                .build());
        log.info("New match {} between {} and {}", mid, bid1.getUid(), bid2.getUid());
        return priorityGroup;
    }
}
