package org.dan.ping.pong.app.castinglots;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.TournamentBid;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.tournament.TournamentInfo;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.util.collection.SetUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

@Slf4j
public class CastingLotsService {
    public static Map<Integer, List<TournamentBid>> groupByCategories(
            List<TournamentBid> bids) {
        return bids.stream().collect(groupingBy(
                TournamentBid::getCid, toList()));
    }

    @Inject
    private CastingLotsDao castingLotsDao;

    @Inject
    private TournamentDao tournamentDao;

    @Transactional(TRANSACTION_MANAGER)
    public void makeGroups(int tid) {
        TournamentInfo tournamentInfo = tournamentDao.lockById(tid)
                .orElseThrow(() -> notFound("Tournament "
                        + tid + " does not exist"));

        if (tournamentInfo.getState() != TournamentState.Draft) {
            throw badRequest("Tournament " + tid
                    + " is not in draft state but "
                    + tournamentInfo.getState());
        }
        makeGroups(tid, tournamentInfo.getMaxGroupSize(),
                tournamentInfo.getQuitesFromGroup());
        tournamentDao.setState(tid, TournamentState.Open);
    }

    @Inject
    private BidDao bidDao;

    @Inject
    private GroupDao groupDao;

    private void makeGroups(int tid, int maxGroupSize, int quits) {
        checkArgument(quits > 0,
                "how much people quits group is wrong");
        log.info("Casting log tournament {}", tid);
        final List<TournamentBid> readyBids = castingLotsDao.findBidsReadyToPlay(tid);
        groupByCategories(readyBids).forEach((cid, bids) -> {
            final double bidsInCategory = bids.size();
            final int groups = max(1, (int) ceil(bidsInCategory / maxGroupSize));
            final int groupSize = (int) ceil(bidsInCategory / groups);
            Iterator<TournamentBid> bidIterator = bids.iterator();
            int basePlayOffPriority = 0;
            for (int gi = 0; gi < groups; ++gi) {
                final int gid = groupDao.createGroup(tid, cid, "Group " + (1 + gi), quits, gi);
                List<TournamentBid> groupBids = SetUtil.firstN(groupSize, bidIterator);
                basePlayOffPriority = Math.max(
                        castingLotsDao.generateGroupMatches(gid, groupBids, tid),
                        basePlayOffPriority);
                bidDao.setGroupForUids(gid, tid, groupBids);
            }
            castingLotsDao.generatePlayOffMatches(tid, cid, groups * quits,
                    basePlayOffPriority + 1);
        });
        log.info("Casting lots for tid {} is complete", tid);
    }
}
