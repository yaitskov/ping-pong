package org.dan.ping.pong.app.castinglots;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.castinglots.FlatCategoryPlayOffBuilder.validateBidsNumberInACategory;
import static org.dan.ping.pong.app.castinglots.PlayOffGenerator.MID0;
import static org.dan.ping.pong.app.group.PlayOffMatcherFromGroup.find;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.match.MatchType.POff;
import static org.dan.ping.pong.app.playoff.PlayOffRule.L1_3P;
import static org.dan.ping.pong.app.playoff.PlayOffService.findLevels;
import static org.dan.ping.pong.app.playoff.PlayOffService.roundPlayOffBase;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.ParticipantLink;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingService;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.group.PlayOffMatcherFromGroup;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.MatchTag;
import org.dan.ping.pong.app.match.MatchType;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.playoff.PlayOffRule;
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
import org.jooq.Select;

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
    private GroupDao groupDao;

    @Inject
    private ParticipantRankingService rankingService;

    @Inject
    private GroupDivider groupDivider;

    @Inject
    private TournamentDaoMySql tournamentDao;

    private void generatePlayOffRules(SelectedCid sCid) {
        sCid.rules().setPlayOff(Optional.of(L1_3P));
        tournamentDao.updateParams(sCid.tid(), sCid.rules(), sCid.batch());
    }

    public Gid addGroup(SelectedCid sCid) {
        if (!sCid.playOff().isPresent()) {
            generatePlayOffRules(sCid);
        }
        final Gid gid = groupService.createGroup(sCid);
        recreatePlayOff(sCid);
        return gid;
    }

    public void seedCategory(SelectedCid sCid, List<ParticipantMemState> readyCatBids) {
        log.info("Begin seeding category {} tournament {}", sCid.cid(), sCid.tid());
        final TournamentRules rules = sCid.tournament().getRule();
        if (!rules.getPlayOff().isPresent()) {
            seedJustTournamentOfOneGroup(sCid, readyCatBids);
        } else if (!rules.getGroup().isPresent()) {
            seedJustPlayOffTournament(sCid, readyCatBids);
        } else {
            seedTournamentWithGroupsAndPlayOff(rules, sCid, readyCatBids);
        }
        log.info("Seeding for tid {} is complete", sCid.tid());
    }

    @Inject
    private DispatchingCategoryPlayOffBuilder categoryPlayOffBuilder;

    @Inject
    private PlayOffService playOffService;

    @Inject
    private DbUpdaterFactory dbUpdaterFactory;

    private void seedJustPlayOffTournament(SelectedCid sCid, List<ParticipantMemState> bids) {
        log.info("Seed cid {} in tid {} as playoff", sCid.cid(), sCid.tid());
        validateBidsNumberInACategory(bids);
        categoryPlayOffBuilder.build(sCid, bids);
    }

    @Inject
    private MatchService matchService;

    @Inject
    private BidService bidService;

    @Inject
    private GroupService groupService;

    private void seedJustTournamentOfOneGroup(
            SelectedCid sCid, List<ParticipantMemState> bids) {
        log.info("Seed cid {} tid {} as group", sCid.cid(), sCid.tid());
        groupService.validateMaxGroupSize(bids.size());
        final List<ParticipantMemState> orderedBids = rankingService.sort(
                bids, sCid.tournament().casting(), sCid.tournament());
        final Gid gid = groupService.createGroup(sCid);
        bidService.setGroupForUids(sCid.batch(), gid, sCid.tid(), orderedBids);
        groupService.generateGroupMatches(sCid.batch(), sCid.tournament(),
                gid, orderedBids, 0, Optional.empty());
    }

    private void seedTournamentWithGroupsAndPlayOff(TournamentRules rules,
            SelectedCid sCid, List<ParticipantMemState> bids) {
        final int quits = rules.getGroup().get().getQuits();
        final Map<Integer, List<ParticipantMemState>> bidsByGroups = groupDivider.divide(
                rules.getCasting(), rules.getGroup().get(),
                rankingService.sort(bids, rules.getCasting(), sCid.tournament()));
        int basePlayOffPriority = 0;
        for (int gi : bidsByGroups.keySet().stream().sorted().collect(toList())) {
            final Gid gid = groupService.createGroup(sCid);
            final List<ParticipantMemState> groupBids = bidsByGroups.get(gi);
            bidService.setGroupForUids(sCid.batch(), gid, sCid.tid(), groupBids);
            if (groupBids.size() > 1) {
                basePlayOffPriority = Math.max(
                        groupService.generateGroupMatches(
                                sCid.batch(), sCid.tournament(), gid, groupBids,
                                0, Optional.empty()),
                        basePlayOffPriority);
            }
        }
        final int playOffStartPositions = roundPlayOffBase(bidsByGroups.size() * quits);
        if (playOffStartPositions > 1) {
            generatePlayOffMatches(sCid, playOffStartPositions, basePlayOffPriority + 1);
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
        tournament.groupBids(participant.getGid())
                .filter(p -> !p.getUid().equals(participant.getUid()))
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

    public void recreatePlayOff(SelectedCid sCid) {
        log.info("Recreate play off in tid {} cid {}", sCid.tid(), sCid.cid());
        final int basePriority = removePlayOffMatchesInCategory(sCid);

        final int quits = sCid.rules().getGroup().get().getQuits();
        final int quittersForRealGroups = sCid.tournament()
                .getGroupsByCategory(sCid.cid()).size() * quits;
        final int roundedQuitters = roundPlayOffBase(quittersForRealGroups);
        generatePlayOffMatches(sCid, roundedQuitters, basePriority);
        if (quittersForRealGroups < roundedQuitters) {
            final List<MatchInfo> playOffMatches = playOffService.findMatchesByLevelAndCid(
                    1, sCid.cid(),
                    sCid.tournament().getMatches().values().stream())
                    .sorted(Comparator.comparing(MatchInfo::getMid))
                    .collect(toList());
            final int realGroups = quittersForRealGroups / quits;
            final int totalGroups = roundedQuitters / quits;
            log.info("Walkover matches with playes from fake groups {}",
                    totalGroups - realGroups);
            for (int iGroup = realGroups; iGroup < totalGroups; ++iGroup) {
                final List<Integer> matchIndexes = find(quits, iGroup, totalGroups);
                for (int iQuitter = 0; iQuitter < quits; ++iQuitter) {
                    int matchIdx = matchIndexes.get(iQuitter);
                    matchService.assignBidToMatch(sCid.tournament(),
                            playOffMatches.get(matchIdx).getMid(),
                            FILLER_LOSER_BID, sCid.batch());
                }
            }
        }
    }

    private int removePlayOffMatchesInCategory(SelectedCid sCid) {
        log.info("Remove play of matches in category {}", sCid.cid());
        final List<MatchInfo> matchesToBeRemoved = sCid.tournament()
                .getMatches().values().stream()
                .filter(m -> m.getCid().equals(sCid.cid()) && !m.getGid().isPresent())
                .collect(toList());

        final Set<Mid> midsForRemoval = matchesToBeRemoved.stream()
                .map(MatchInfo::getMid).collect(toSet());
        filterMatches(sCid.tournament(), midsForRemoval);
        matchDao.deleteByIds(sCid.tid(), midsForRemoval, sCid.batch());
        return matchesToBeRemoved.stream()
                .map(MatchInfo::getPriority)
                .min(Integer::compare)
                .orElse(0);
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

    public Mid generatePlayOffMatches(SelectedCid sCid,
            int playOffStartPositions, int basePlayOffPriority) {
        final PlayOffGenerator generator = createPlayOffGen(sCid, empty(), 0, Gold);
        return generatePlayOffMatches(generator, playOffStartPositions,
                basePlayOffPriority, empty(), 0);
    }

    public Mid generatePlayOffMatches(PlayOffGenerator generator,
            int playOffStartPositions, int basePlayOffPriority,
            Optional<Mid> parentMid, int baseLevel) {
        final Tid tid = generator.getTournament().getTid();
        log.info("Generate play off matches for {} bids in tid {}",
                playOffStartPositions, tid);
        if (playOffStartPositions == 1) {
            throw internalError("Tournament " + tid + ":" + generator.getCid()
                    + " will be without play off");
        } else {
            checkArgument(playOffStartPositions > 0, "not enough groups %s",
                    playOffStartPositions);
            checkArgument(playOffStartPositions % 2 == 0, "odd number groups %s",
                    playOffStartPositions);
        }
        final int levels = findLevels(playOffStartPositions) + baseLevel;
        final int lowestPriority = basePlayOffPriority + levels;
        final PlayOffRule playOffRule = generator.getTournament().getRule().getPlayOff()
                .orElseThrow(() -> internalError("no play off rule in " + tid));
        switch (playOffRule.getLosings()) {
            case 1:
                return generator.generateTree(levels, parentMid, lowestPriority,
                        TypeChain.of(generator.getGoldType(), POff), empty()).get();
            case 2:
                return generator.generate2LossTree(2 * levels, lowestPriority, parentMid).get();
            default:
                throw internalError("unsupported number of losings "
                        + playOffRule.getLosings() + " in " + tid + " ");
        }
    }

    public PlayOffGenerator createPlayOffGen(
            SelectedCid sCid, Optional<MatchTag> tag, int baseLevel, MatchType goldType) {
        return PlayOffGenerator.builder()
                .sCid(sCid)
                .matchService(matchService)
                .goldType(goldType)
                .tag(tag)
                .finalLevel(1 + baseLevel)
                .build();
    }
}
