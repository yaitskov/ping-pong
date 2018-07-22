package org.dan.ping.pong.app.castinglots;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.castinglots.FlatCategoryPlayOffBuilder.validateBidsNumberInACategory;
import static org.dan.ping.pong.app.castinglots.PlayOffGenerator.FIRST_PLAY_OFF_MATCH_LEVEL;
import static org.dan.ping.pong.app.match.MatchService.roundPlayOffBase;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.playoff.PlayOffRule.L1_3P;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.ParticipantLink;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.group.PlayOffMatcherFromGroup;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.sched.ScheduleServiceSelector;
import org.dan.ping.pong.app.tournament.DbUpdaterFactory;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentDaoMySql;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class CastingLotsService {
    private static final ImmutableSet<BidState> WANT_PAID_HERE = ImmutableSet.of(Want, Paid, Here);
    private static final ImmutableSet<BidState> WANT_PAID = ImmutableSet.of(Want, Paid);
    private static final ImmutableSet<BidState> QUIT_EXPELLED = ImmutableSet.of(Quit, Expl);

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

    public Gid addGroup(TournamentMemState tournament, DbUpdater batch, Cid cid) {
        if (!tournament.getRule().getPlayOff().isPresent()) {
            generatePlayOffRules(tournament, batch);
        }
        final Gid gid = groupService.createGroup(tournament, cid, batch);
        recreatePlayOff(tournament, cid, batch);
        return gid;
    }

    public void seedCategory(TournamentMemState tournament,
            Cid cid, List<ParticipantMemState> readyCatBids, DbUpdater batch) {
        log.info("Begin seeding category {} tournament {}", cid, tournament.getTid());
        final TournamentRules rules = tournament.getRule();
        if (!rules.getPlayOff().isPresent()) {
            seedJustTournamentOfOneGroup(rules, tournament, cid, readyCatBids, batch);
        } else if (!rules.getGroup().isPresent()) {
            seedJustPlayOffTournament(tournament, cid, readyCatBids, batch);
        } else {
            seedTournamentWithGroupsAndPlayOff(rules, cid, readyCatBids, tournament, batch);
        }
        log.info("Seeding for tid {} is complete", tournament.getTid());
    }

    @Inject
    private DispatchingCategoryPlayOffBuilder categoryPlayOffBuilder;

    @Inject
    private PlayOffService playOffService;

    @Inject
    private DbUpdaterFactory dbUpdaterFactory;

    private void seedJustPlayOffTournament(TournamentMemState tournament,
            Cid cid, List<ParticipantMemState> bids, DbUpdater batch) {
        log.info("Seed cid {} in tid {} as playoff", cid, tournament.getTid());
        validateBidsNumberInACategory(bids);
        categoryPlayOffBuilder.build(tournament, cid, bids, batch);
    }

    @Inject
    private MatchService matchService;

    @Inject
    private BidService bidService;

    private static final Set<BidState> terminalBidState = ImmutableSet.of(Expl, Quit);

    @Inject
    private GroupService groupService;

    private void seedJustTournamentOfOneGroup(TournamentRules rules,
            TournamentMemState tournament,
            Cid cid, List<ParticipantMemState> bids, DbUpdater batch) {
        log.info("Seed cid {} tid {} as group", cid, tournament.getTid());
        final Tid tid = tournament.getTid();
        groupService.validateMaxGroupSize(bids.size());
        final List<ParticipantMemState> orderedBids = rankingService.sort(
                bids, rules.getCasting(), tournament);
        final Gid gid = groupService.createGroup(tournament, cid, batch);
        orderedBids.forEach(bid -> bid.setGid(Optional.of(gid)));
        groupService.generateGroupMatches(batch, tournament, gid, orderedBids,
                0, Optional.empty());
        bidDao.setGroupForUids(batch, gid, tid, orderedBids);
    }

    private void seedTournamentWithGroupsAndPlayOff(TournamentRules rules,
            Cid cid, List<ParticipantMemState> bids,
            TournamentMemState tournament, DbUpdater batch) {
        final Tid tid = tournament.getTid();
        final int quits = rules.getGroup().get().getQuits();
        final Map<Integer, List<ParticipantMemState>> bidsByGroups = groupDivider.divide(
                rules.getCasting(), rules.getGroup().get(),
                rankingService.sort(bids, rules.getCasting(), tournament));
        int basePlayOffPriority = 0;
        for (int gi : bidsByGroups.keySet().stream().sorted().collect(toList())) {
            final Gid gid = groupService.createGroup(tournament, cid, batch);
            final List<ParticipantMemState> groupBids = bidsByGroups.get(gi);
            groupBids.forEach(bid -> bid.setGid(Optional.of(gid)));
            if (groupBids.size() > 1) {
                basePlayOffPriority = Math.max(
                        groupService.generateGroupMatches(batch, tournament, gid, groupBids,
                                0, Optional.empty()),
                        basePlayOffPriority);
            }
            bidDao.setGroupForUids(batch, gid, tid, groupBids);
        }
        final int playOffStartPositions = roundPlayOffBase(bidsByGroups.size() * quits);
        if (playOffStartPositions > 1) {
            castingLotsDao.generatePlayOffMatches(batch, tournament, cid,
                    playOffStartPositions, basePlayOffPriority + 1);
        }
    }

    public List<ParticipantMemState> findBidsReadyToPlay(TournamentMemState tournament) {
        return tournament.getParticipants().values().stream()
                .filter(bid -> WANT_PAID_HERE.contains(bid.getBidState()))
                .collect(toList());
    }

    public void checkAllThatAllHere(List<ParticipantMemState> readyBids) {
        final List<ParticipantLink> notHere = readyBids.stream()
                .filter(bid -> WANT_PAID.contains(bid.state()))
                .map(ParticipantMemState::toBidLink)
                .collect(toList());
        if (notHere.isEmpty()) {
            return;
        }
        throw badRequest(new UncheckedParticipantsError(notHere));
    }

    public void orderCategoryBidsManually(OrderCategoryBidsManually order) {
        castingLotsDao.orderCategoryBidsManually(order);
    }

    public List<RankedBid> loadManualBidsOrder(Tid tid, Cid cid) {
        return castingLotsDao.loadManualBidsOrder(tid, cid);
    }

    @Inject
    private ScheduleServiceSelector scheduleService;

    @Inject
    private Clocker clocker;

    public void addParticipant(Bid bid, TournamentMemState tournament, DbUpdater batch) {
        final ParticipantMemState participant = tournament.getParticipant(bid);
        log.info("Add participant {} to group", bid, participant.getGid());
        int[] priority = new int[1];
        tournament.getParticipants().values().stream()
                .filter(p -> p.getGid().equals(participant.getGid())
                        && !p.getUid().equals(participant.getUid()))
                .sorted(Comparator.comparingInt(p -> p.getUid().getId()))
                .forEach(
                        p -> {
                            final boolean isDeadParticipant = QUIT_EXPELLED.contains(p.state());
                            priority[0] = groupService.addGroupMatch(
                                    batch,
                                    tournament, priority[0], participant, p,
                                    isDeadParticipant
                                            ? Over
                                            : Place,
                                    isDeadParticipant
                                            ? Optional.of(bid)
                                            : Optional.empty(),
                                    Optional.empty());
                        });
        scheduleService.afterMatchComplete(tournament, batch,  clocker.get());
    }

    public void recreatePlayOff(TournamentMemState tournament, Cid cid, DbUpdater batch) {
        log.info("Recreate play off in tid {} cid {}", tournament.getTid(), cid);
        final int basePriority = removePlayOffMatchesInCategory(tournament, cid, batch);

        final int quits = tournament.getRule().getGroup().get().getQuits();
        final int quittersForRealGroups = tournament.getGroupsByCategory(cid).size() * quits;
        final int roundedQuitters = roundPlayOffBase(quittersForRealGroups);
        castingLotsDao.generatePlayOffMatches(batch, tournament, cid,
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
                            FILLER_LOSER_BID, batch);
                }
            }
        }
    }

    private int removePlayOffMatchesInCategory(TournamentMemState tournament, Cid cid, DbUpdater batch) {
        log.info("Remove play of matches in category {}", cid);
        final List<MatchInfo> matchesToBeRemoved = tournament
                .getMatches().values().stream()
                .filter(m -> m.getCid().equals(cid) && !m.getGid().isPresent())
                .collect(toList());

        final Set<Mid> midsForRemoval = matchesToBeRemoved.stream()
                .map(MatchInfo::getMid).collect(toSet());
        filterMatches(tournament, midsForRemoval);
        matchDao.deleteByIds(tournament.getTid(), midsForRemoval, batch);
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
