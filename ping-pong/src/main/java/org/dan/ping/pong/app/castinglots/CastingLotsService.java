package org.dan.ping.pong.app.castinglots;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.castinglots.PlayOffGenerator.PLAY_OFF_SEEDS;
import static org.dan.ping.pong.app.sched.ScheduleCtx.SCHEDULE_SELECTOR;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.createLoserBid;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.sched.ScheduleService;
import org.dan.ping.pong.app.tournament.DbUpdaterFactory;
import org.dan.ping.pong.app.tournament.Uid;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.sys.db.DbUpdaterSql;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class CastingLotsService {
    public static final String NOT_ENOUGH_PARTICIPANTS = "Not enough participants";
    public static final String N = "n";

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

    @Transactional(TRANSACTION_MANAGER)
    public void seed(OpenTournamentMemState tournament) {
        log.info("Begin seeding tournament {}", tournament.getTid());
        final TournamentRules rules = tournament.getRule();
        final List<ParticipantMemState> readyBids = findBidsReadyToPlay(tournament);
        checkAllThatAllHere(readyBids);
        checkAtLeast(readyBids);
        if (!rules.getPlayOff().isPresent()) {
            seedJustGroupTournament(rules, tournament, readyBids);
        } else if (!rules.getGroup().isPresent()) {
            seedJustPlayOffTournament(rules, tournament, readyBids);
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
            OpenTournamentMemState tournament,
            List<ParticipantMemState> readyBids) {
        log.info("Seed tid {} as playoff", tournament.getTid());
        groupByCategories(readyBids).forEach((Integer cid, List<ParticipantMemState> bids) -> {
            validateBidsNumberInACategory(bids);
            final List<ParticipantMemState> orderedBids = rankingService.sort(bids, rules.getCasting());
            final int basePositions = matchService.roundPlayOffBase(orderedBids.size());
            castingLotsDao.generatePlayOffMatches(tournament, cid, basePositions, 1);
            final DbUpdaterSql updater = dbUpdaterFactory.create();
            assignBidsToBaseMatches(cid, basePositions, orderedBids, tournament, updater);
            updater.flush();
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

    private void assignBidsToBaseMatches(Integer cid, int basePositions,
            List<ParticipantMemState> orderedBids,
            OpenTournamentMemState tournament, DbUpdaterSql batch) {
        final List<Integer> seeds = ofNullable(PLAY_OFF_SEEDS.get(basePositions))
                .orElseThrow(() -> internalError("No seeding for "
                        + orderedBids.size() + " participants"));

        final List<MatchInfo> baseMatches = playOffService.findBaseMatches(
                playOffService.findPlayOffMatches(tournament, cid))
                .stream()
                .sorted(Comparator.comparingInt(MatchInfo::getMid))
                .collect(toList());

        for (int iMatch = 0; iMatch < basePositions / 2; ++iMatch) {
            final MatchInfo match = baseMatches.get(iMatch);
            final int iBid1 = seeds.get(iMatch * 2);
            final int iBid2 = seeds.get(iMatch * 2 + 1);
            final int iStrongBid = Math.min(iBid1, iBid2);
            final int iWeakBid = Math.max(iBid1, iBid2);

            matchService.assignBidToMatch(tournament, match.getMid(),
                    orderedBids.get(iStrongBid).getUid(), batch);

            if (iWeakBid >= orderedBids.size()) {
                final ParticipantMemState fakeLoser = createLoserBid(
                        new Tid(tournament.getTid()), cid);
                bidService.setBidState(orderedBids.get(iStrongBid), Play, singletonList(Here), batch);
                matchService.assignBidToMatch(tournament, match.getMid(),
                        fakeLoser.getUid(), batch);
                matchService.leaveFromPlayOff(fakeLoser, tournament, batch);
            } else {
                matchService.assignBidToMatch(tournament, match.getMid(),
                        orderedBids.get(iWeakBid).getUid(), batch);
            }
        }
    }

    private void seedJustGroupTournament(TournamentRules rules,
            OpenTournamentMemState tournament,
            List<ParticipantMemState> readyBids) {
        log.info("Seed tid {} as group", tournament.getTid());
        final int tid = tournament.getTid();
        final int quits = rules.getGroup().get().getQuits();
        groupByCategories(readyBids).forEach((Integer cid, List<ParticipantMemState> bids) -> {
            validateBidsNumberInACategory(bids);
            final List<ParticipantMemState> orderedBids = rankingService.sort(bids, rules.getCasting());
            final String groupLabel = "Group 1";
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
            OpenTournamentMemState tournament, List<ParticipantMemState> readyBids) {
        final int tid = tournament.getTid();
        final int quits = rules.getGroup().get().getQuits();
        groupByCategories(readyBids).forEach((Integer cid, List<ParticipantMemState> bids) -> {
            validateBidsNumberInACategory(bids);
            final Map<Integer, List<ParticipantMemState>> bidsByGroups = groupDivider.divide(
                    rules.getCasting(), rules.getGroup().get(),
                    rankingService.sort(bids, rules.getCasting()));
            int basePlayOffPriority = 0;
            for (int gi : bidsByGroups.keySet().stream().sorted().collect(toList())) {
                final String groupLabel = "Group " + (1 + gi);
                final int gid = groupDao.createGroup(tid, cid, groupLabel, quits, gi);
                tournament.getGroups().put(gid, GroupInfo.builder().gid(gid).cid(cid)
                        .ordNumber(gi).label(groupLabel).build());
                final List<ParticipantMemState> groupBids = bidsByGroups.get(gi);
                if (groupBids.size() <= quits) {
                    throw badRequest("Category should have more participants than quits from a group");
                }
                groupBids.forEach(bid -> bid.setGid(Optional.of(gid)));
                basePlayOffPriority = Math.max(
                        castingLotsDao.generateGroupMatches(tournament, gid, groupBids, 0),
                        basePlayOffPriority);
                bidDao.setGroupForUids(gid, tid, groupBids);
            }
            castingLotsDao.generatePlayOffMatches(tournament, cid,
                    matchService.roundPlayOffBase(bidsByGroups.size() * quits),
                    basePlayOffPriority + 1);
        });
    }

    private void checkAtLeast(List<ParticipantMemState> readyBids) {
        if (readyBids.size() < 2) {
            throw badRequest(NOT_ENOUGH_PARTICIPANTS, N, readyBids.size());
        }
    }

    private List<ParticipantMemState> findBidsReadyToPlay(OpenTournamentMemState tournament) {
        return tournament.getParticipants().values().stream()
                .filter(bid -> ImmutableSet.of(Want, Paid, Here).contains(bid.getBidState()))
                .collect(toList());
    }

    private void checkAllThatAllHere(List<ParticipantMemState> readyBids) {
        final List<UserLink> notHere = readyBids.stream().filter(bid ->
                bid.getState() == Want || bid.getState() == Paid)
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

    public List<RankedBid> loadManualBidsOrder(int tid, int cid) {
        return castingLotsDao.loadManualBidsOrder(tid, cid);
    }

    @Inject
    @Named(SCHEDULE_SELECTOR)
    private ScheduleService scheduleService;

    @Inject
    private Clocker clocker;

    public void addParticipant(Uid uid, OpenTournamentMemState tournament, DbUpdater batch) {
        final ParticipantMemState participant = tournament.getParticipant(uid);
        log.info("Add participant {} to group", uid, participant.getGid());
        int[] priority = new int[1];
        tournament.getParticipants().values().stream()
                .filter(p -> p.getGid().equals(participant.getGid())
                        && !p.getUid().equals(participant.getUid()))
                .sorted(Comparator.comparingInt(p -> p.getUid().getId()))
                .forEach(
                        p -> priority[0] = castingLotsDao.addGroupMatch(
                                tournament, priority[0], participant, p));
        scheduleService.afterMatchComplete(tournament, batch,  clocker.get());
    }
}
