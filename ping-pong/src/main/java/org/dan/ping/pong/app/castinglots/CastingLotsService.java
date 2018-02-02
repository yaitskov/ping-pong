package org.dan.ping.pong.app.castinglots;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.castinglots.PlayOffGenerator.FIRST_PLAY_OFF_MATCH_LEVEL;
import static org.dan.ping.pong.app.castinglots.PlayOffGenerator.PLAY_OFF_SEEDS;
import static org.dan.ping.pong.app.match.MatchService.roundPlayOffBase;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.playoff.PlayOffRule.L1_3P;
import static org.dan.ping.pong.app.sched.ScheduleCtx.SCHEDULE_SELECTOR;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_UID;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.group.PlayOffMatcherFromGroup;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.sched.ScheduleService;
import org.dan.ping.pong.app.tournament.DbUpdaterFactory;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentDaoMySql;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class CastingLotsService {
    public static final String NOT_ENOUGH_PARTICIPANTS = "Not enough participants";
    public static final String N = "n";
    private static final ImmutableSet<BidState> WANT_PAID_HERE = ImmutableSet.of(Want, Paid, Here);
    private static final ImmutableSet<BidState> WANT_PAID = ImmutableSet.of(Want, Paid);
    private static final ImmutableSet<BidState> QUIT_EXPELLED = ImmutableSet.of(Quit, Expl);

    public static Map<Integer, List<ParticipantMemState>> groupByCategories(
            List<ParticipantMemState> bids) {
        return bids.stream().collect(groupingBy(
                ParticipantMemState::getCid, toList()));
    }

    @Inject
    private CastingLotsDao castingLotsDao;

    @Inject
    private BidDao bidDao;

    @Inject
    private GroupDao groupDao;

    @Inject
    private ParticipantRankingService rankingService;

    @Inject
    private GroupDivider groupDivider;

    @Inject
    private TournamentDaoMySql tournamentDao;

    private void generatePlayOffRules(TournamentMemState tournament, DbUpdater batch) {
        tournament.getRule().setPlayOff(Optional.of(L1_3P));
        tournamentDao.updateParams(tournament.getTid(), tournament.getRule(), batch);
    }

    public int addGroup(TournamentMemState tournament, DbUpdater batch, int cid) {
        if (!tournament.getRule().getPlayOff().isPresent()) {
            generatePlayOffRules(tournament, batch);
        }
        final int gid = groupService.createGroup(tournament, cid);
        recreatePlayOff(tournament, cid, batch);
        return gid;
    }

    public void seed(TournamentMemState tournament, DbUpdater batch) {
        log.info("Begin seeding tournament {}", tournament.getTid());
        final TournamentRules rules = tournament.getRule();
        final List<ParticipantMemState> readyBids = findBidsReadyToPlay(tournament);
        checkAllThatAllHere(readyBids);
        checkAtLeast(readyBids);
        if (!rules.getPlayOff().isPresent()) {
            seedJustGroupTournament(rules, tournament, readyBids);
        } else if (!rules.getGroup().isPresent()) {
            seedJustPlayOffTournament(rules, tournament, readyBids, batch);
        } else {
            seedTournamentWithGroupsAndPlayOff(rules, tournament, readyBids);
        }
        log.info("Seeding for tid {} is complete", tournament.getTid());
    }

    @Inject
    private PlayOffService playOffService;

    @Inject
    private DbUpdaterFactory dbUpdaterFactory;

    private void seedJustPlayOffTournament(TournamentRules rules,
            TournamentMemState tournament,
            List<ParticipantMemState> readyBids, DbUpdater batch) {
        log.info("Seed tid {} as playoff", tournament.getTid());
        groupByCategories(readyBids).forEach((Integer cid, List<ParticipantMemState> bids) -> {
            validateBidsNumberInACategory(bids);
            final List<ParticipantMemState> orderedBids = rankingService.sort(bids, rules.getCasting(), tournament);
            final int basePositions = roundPlayOffBase(orderedBids.size());
            castingLotsDao.generatePlayOffMatches(tournament, cid, basePositions, 1);
            assignBidsToBaseMatches(cid, basePositions, orderedBids, tournament, batch);
        });
    }

    private void validateBidsNumberInACategory(List<ParticipantMemState> bids) {
        if (bids.size() < 2) {
            throw badRequest("There is a category with 1 participant."
                    + " Expel him/her or move into another category.");
        }
        if (bids.size() > 128) {
            throw badRequest("Category has more than 128 participants");
        }
    }

    @Inject
    private MatchService matchService;

    @Inject
    private BidService bidService;

    private static final Set<BidState> terminalBidState = ImmutableSet.of(Expl, Quit);

    private void assignBidsToBaseMatches(Integer cid, int basePositions,
            List<ParticipantMemState> orderedBids,
            TournamentMemState tournament, DbUpdater batch) {
        final List<Integer> seeds = ofNullable(PLAY_OFF_SEEDS.get(basePositions))
                .orElseThrow(() -> internalError("No seeding for "
                        + orderedBids.size() + " participants"));

        final List<MatchInfo> baseMatches = playOffService
                .findBaseMatches(tournament, cid)
                .stream()
                .sorted(Comparator.comparing(MatchInfo::getMid))
                .collect(toList());

        for (int iMatch = 0; iMatch < basePositions / 2; ++iMatch) {
            final MatchInfo match = baseMatches.get(iMatch);
            final int iBid1 = seeds.get(iMatch * 2);
            final int iBid2 = seeds.get(iMatch * 2 + 1);
            final int iStrongBid = Math.min(iBid1, iBid2);
            final int iWeakBid = Math.max(iBid1, iBid2);

            final ParticipantMemState strongBid = orderedBids.get(iStrongBid);
            matchService.assignBidToMatch(tournament, match.getMid(),
                    strongBid.getUid(), batch);

            if (iWeakBid >= orderedBids.size()) {
                matchService.assignBidToMatch(tournament, match.getMid(), FILLER_LOSER_UID, batch);
            } else {
                final ParticipantMemState weakBid = orderedBids.get(iWeakBid);
                matchService.assignBidToMatch(tournament, match.getMid(), weakBid.getUid(), batch);
            }
        }
    }

    @Inject
    private GroupService groupService;

    private void seedJustGroupTournament(TournamentRules rules,
            TournamentMemState tournament,
            List<ParticipantMemState> readyBids) {
        log.info("Seed tid {} as group", tournament.getTid());
        final Tid tid = tournament.getTid();
        final int quits = rules.getGroup().get().getQuits();
        groupByCategories(readyBids).forEach((Integer cid, List<ParticipantMemState> bids) -> {
            validateBidsNumberInACategory(bids);
            final List<ParticipantMemState> orderedBids = rankingService.sort(
                    bids, rules.getCasting(), tournament);
            final String groupLabel = GroupService.sortToLabel(0);
            final int groupIdx = 0;
            final int gid = groupDao.createGroup(tid, cid, groupLabel, quits, groupIdx);
            tournament.getGroups().put(gid, GroupInfo.builder().gid(gid).cid(cid)
                    .ordNumber(groupIdx).label(groupLabel).build());
            orderedBids.forEach(bid -> bid.setGid(Optional.of(gid)));
            castingLotsDao.generateGroupMatches(tournament, gid, orderedBids, groupIdx);
            bidDao.setGroupForUids(gid, tid, orderedBids);
        });
    }

    private void seedTournamentWithGroupsAndPlayOff(TournamentRules rules,
            TournamentMemState tournament, List<ParticipantMemState> readyBids) {
        final Tid tid = tournament.getTid();
        final int quits = rules.getGroup().get().getQuits();
        groupByCategories(readyBids).forEach((Integer cid, List<ParticipantMemState> bids) -> {
            validateBidsNumberInACategory(bids);
            final Map<Integer, List<ParticipantMemState>> bidsByGroups = groupDivider.divide(
                    rules.getCasting(), rules.getGroup().get(),
                    rankingService.sort(bids, rules.getCasting(), tournament));
            int basePlayOffPriority = 0;
            for (int gi : bidsByGroups.keySet().stream().sorted().collect(toList())) {
                final String groupLabel = GroupService.sortToLabel(gi);
                final int gid = groupDao.createGroup(tid, cid, groupLabel, quits, gi);
                tournament.getGroups().put(gid, GroupInfo.builder().gid(gid).cid(cid)
                        .ordNumber(gi).label(groupLabel).build());
                final List<ParticipantMemState> groupBids = bidsByGroups.get(gi);
                if (groupBids.size() < quits) {
                    throw badRequest("Category should have more participants than quits from a group");
                }
                groupBids.forEach(bid -> bid.setGid(Optional.of(gid)));
                basePlayOffPriority = Math.max(
                        castingLotsDao.generateGroupMatches(tournament, gid, groupBids, 0),
                        basePlayOffPriority);
                bidDao.setGroupForUids(gid, tid, groupBids);
            }
            castingLotsDao.generatePlayOffMatches(tournament, cid,
                    roundPlayOffBase(bidsByGroups.size() * quits),
                    basePlayOffPriority + 1);
        });
    }

    private void checkAtLeast(List<ParticipantMemState> readyBids) {
        if (readyBids.size() < 2) {
            throw badRequest(NOT_ENOUGH_PARTICIPANTS, N, readyBids.size());
        }
    }

    private List<ParticipantMemState> findBidsReadyToPlay(TournamentMemState tournament) {
        return tournament.getParticipants().values().stream()
                .filter(bid -> WANT_PAID_HERE.contains(bid.getBidState()))
                .collect(toList());
    }

    private void checkAllThatAllHere(List<ParticipantMemState> readyBids) {
        final List<UserLink> notHere = readyBids.stream()
                .filter(bid -> WANT_PAID.contains(bid.getState()))
                .map(ParticipantMemState::toLink)
                .collect(toList());
        if (notHere.isEmpty()) {
            return;
        }
        throw badRequest(new UncheckedParticipantsError(notHere));
    }

    public void orderCategoryBidsManually(OrderCategoryBidsManually order) {
        castingLotsDao.orderCategoryBidsManually(order);
    }

    public List<RankedBid> loadManualBidsOrder(Tid tid, int cid) {
        return castingLotsDao.loadManualBidsOrder(tid, cid);
    }

    @Inject
    @Named(SCHEDULE_SELECTOR)
    private ScheduleService scheduleService;

    @Inject
    private Clocker clocker;

    public void addParticipant(Uid uid, TournamentMemState tournament, DbUpdater batch) {
        final ParticipantMemState participant = tournament.getParticipant(uid);
        log.info("Add participant {} to group", uid, participant.getGid());
        int[] priority = new int[1];
        tournament.getParticipants().values().stream()
                .filter(p -> p.getGid().equals(participant.getGid())
                        && !p.getUid().equals(participant.getUid()))
                .sorted(Comparator.comparingInt(p -> p.getUid().getId()))
                .forEach(
                        p -> {
                            final boolean isDeadParticipant = QUIT_EXPELLED.contains(p.getState());
                            priority[0] = castingLotsDao.addGroupMatch(
                                    tournament, priority[0], participant, p,
                                    isDeadParticipant
                                            ? Over
                                            : Place,
                                    isDeadParticipant
                                            ? Optional.of(uid)
                                            : Optional.empty());
                        });
        scheduleService.afterMatchComplete(tournament, batch,  clocker.get());
    }

    public void recreatePlayOff(TournamentMemState tournament, int cid, DbUpdater batch) {
        log.info("Recreate play off in tid {} cid {}", tournament.getTid(), cid);
        final int basePriority = removePlayOffMatchesInCategory(tournament, cid, batch);

        final int quits = tournament.getRule().getGroup().get().getQuits();
        final int quittersForRealGroups = tournament.getGroupsByCategory(cid).size() * quits;
        final int roundedQuitters = roundPlayOffBase(quittersForRealGroups);
        castingLotsDao.generatePlayOffMatches(tournament, cid,
                roundedQuitters, basePriority);
        if (quittersForRealGroups < roundedQuitters) {
            final List<MatchInfo> playOffMatches = playOffService.findMatchesByLevelAndCid(
                    FIRST_PLAY_OFF_MATCH_LEVEL, cid,
                    tournament.getMatches().values().stream())
                    .sorted(Comparator.comparing(MatchInfo::getMid))
                    .collect(toList());
            final int realGroups = quittersForRealGroups / quits;
            final int totalGroups = roundedQuitters / quits;
            log.info("Walkover matches with playes from fake groups {}", totalGroups - realGroups);
            for (int iGroup = realGroups; iGroup < totalGroups; ++iGroup) {
                final List<Integer> matchIndexes = PlayOffMatcherFromGroup.find(quits, iGroup, totalGroups);
                for (int iQuitter = 0; iQuitter < quits; ++iQuitter) {
                    int matchIdx = matchIndexes.get(iQuitter);
                    matchService.assignBidToMatch(tournament,
                            playOffMatches.get(matchIdx).getMid(),
                            FILLER_LOSER_UID, batch);
                }
            }
        }
    }

    private int removePlayOffMatchesInCategory(TournamentMemState tournament, int cid, DbUpdater batch) {
        log.info("Remove play of matches in category {}", cid);
        final List<MatchInfo> matchesToBeRemoved = tournament
                .getMatches().values().stream()
                .filter(m -> m.getCid() == cid && !m.getGid().isPresent())
                .collect(toList());

        final Set<Mid> midsForRemoval = matchesToBeRemoved.stream()
                .map(MatchInfo::getMid).collect(toSet());
        filterMatches(tournament, midsForRemoval);
        matchDao.deleteByIds(midsForRemoval, batch);
        return matchesToBeRemoved.stream().map(MatchInfo::getPriority).min(Integer::compare).orElse(0);
    }

    @Inject
    private MatchDao matchDao;

    private void filterMatches(TournamentMemState tournament, Set<Mid> midsForRemoval) {
        tournament.setMatches(tournament.getMatches()
                .values()
                .stream()
                .filter(m -> !midsForRemoval.contains(m.getMid()))
                .collect(toMap(MatchInfo::getMid, m -> m)));
    }
}
