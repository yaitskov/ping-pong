package org.dan.ping.pong.app.castinglots;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.util.collection.SetUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

@Slf4j
public class CastingLotsService {
    public static Map<Integer, List<ParticipantMemState>> groupByCategories(
            List<ParticipantMemState> bids) {
        return bids.stream().collect(groupingBy(
                ParticipantMemState::getCid, toList()));
    }

    @Inject
    private CastingLotsDao castingLotsDao;

    @Inject
    private TournamentDao tournamentDao;

    @Inject
    private BidDao bidDao;

    @Inject
    private GroupDao groupDao;

    @Transactional(TRANSACTION_MANAGER)
    public void makeGroups(OpenTournamentMemState tournament) {
        final int tid = tournament.getTid();
        final int quits = tournament.getRule().getGroup().getQuits();
        checkArgument(quits > 0,
                "how much people quits group is wrong");
        log.info("Casting log tournament {}", tournament.getTid());
        final List<ParticipantMemState> readyBids = findBidsReadyToPlay(tournament);
        checkAllThatAllHere(readyBids);
        groupByCategories(readyBids).forEach((Integer cid, List<ParticipantMemState> bids) -> {
            if (bids.size() < 2) {
                throw badRequest("There is a category with 1 participant."
                        + " Expel him/her or move into another category.");
            }
            bids = bids.stream().sorted(Comparator.comparingInt(bid -> bid.getUid().getId())).collect(toList());
            final double bidsInCategory = bids.size();
            final int groups = max(1, (int) ceil(bidsInCategory
                    / tournament.getRule().getGroup().getMaxSize()));
            final int groupSize = (int) ceil(bidsInCategory / groups);
            Iterator<ParticipantMemState> bidIterator = bids.iterator();
            int basePlayOffPriority = 0;
            for (int gi = 0; gi < groups; ++gi) {
                final String groupLabel = "Group " + (1 + gi);
                final int gid = groupDao.createGroup(tid, cid, groupLabel, quits, gi);
                tournament.getGroups().put(gid, GroupInfo.builder().gid(gid).cid(cid)
                        .ordNumber(gi).label(groupLabel).build());
                List<ParticipantMemState> groupBids = SetUtil.firstN(groupSize, bidIterator);
                if (groupBids.size() <= quits) {
                    throw badRequest("Category should have more participants than quits from a group");
                }
                basePlayOffPriority = Math.max(
                        castingLotsDao.generateGroupMatches(tournament, gid, groupBids),
                        basePlayOffPriority);
                bidDao.setGroupForUids(gid, tid, groupBids);
            }
            castingLotsDao.generatePlayOffMatches(tournament, cid, groups * quits,
                    basePlayOffPriority + 1);
        });
        log.info("Casting lots for tid {} is scoreSetAndCompleteIfWinOrLose", tid);
    }

    private List<ParticipantMemState> findBidsReadyToPlay(OpenTournamentMemState tournament) {
        return tournament.getParticipants().values().stream()
                .filter(bid -> ImmutableSet.of(Want, Paid, Here).contains(bid.getBidState()))
                .collect(toList());
    }

    @Inject
    private UserDao userDao;

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
}
