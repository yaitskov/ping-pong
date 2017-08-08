package org.dan.ping.pong.app.castinglots;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.log;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ord.dan.ping.pong.jooq.Tables.BID;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.match.MatchType.POff;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.TournamentBid;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.score.MatchScoreDao;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

@Slf4j
public class CastingLotsDao {
    @Inject
    private DSLContext jooq;

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<TournamentBid> findBidsReadyToPlay(int tid) {
        return jooq.select(BID.UID, BID.CID).from(BID)
                .where(BID.TID.eq(tid), BID.STATE.eq(Here))
                .orderBy(BID.UID)
                .fetch()
                .map(r -> TournamentBid
                        .builder()
                        .uid(r.get(BID.UID))
                        .cid(r.get(BID.CID))
                        .build());
    }

    @Inject
    private MatchDao matchDao;

    @Inject
    private MatchScoreDao matchScoreDao;

    @Transactional(TRANSACTION_MANAGER)
    public int generateGroupMatches(int gid, List<TournamentBid> groupBids, int tid) {
        CastingLotsDao.log.info("Generate matches for group {} in tournament {}", gid, tid);
        int priorityGroup = 0;
        for (int i = 0; i < groupBids.size(); ++i) {
            final TournamentBid bid1 = groupBids.get(i);
            for (int j = i + 1; j < groupBids.size(); ++j) {
                final TournamentBid bid2 = groupBids.get(j);
                final int mid = matchDao.createGroupMatch(tid,
                        gid, bid1.getCid(), ++priorityGroup);
                matchScoreDao.createScore(mid, bid1.getUid(), bid1.getCid(), tid);
                matchScoreDao.createScore(mid, bid2.getUid(), bid2.getCid(), tid);
                CastingLotsDao.log.info("New match {} between {} and {}", mid,
                        bid1.getUid(), bid2.getUid());
            }
        }
        return priorityGroup;
    }

    @Transactional(TRANSACTION_MANAGER)
    public int generatePlayOffMatches(int tid, Integer cid,
            int playOffStartPositions, int basePlayOffPriority) {
        CastingLotsDao.log.info("Generate play off matches for {} groups in tournament {}",
                playOffStartPositions, tid);
        if (playOffStartPositions == 1) {
            CastingLotsDao.log.info("Tournament {}:{} will be without play off", tid, cid);
            return 0;
        } else {
            checkArgument(playOffStartPositions > 0, "not enough groups %s", playOffStartPositions);
            checkArgument(playOffStartPositions % 2 == 0, "odd number groups %s", playOffStartPositions);
        }
        final int levels = (int) (log(playOffStartPositions) / log(2)) - 1;
        final int lowestPriority = basePlayOffPriority + levels;
        final int firstPlaceMid = matchDao.createPlayOffMatch(tid, cid, empty(), empty(),
                lowestPriority, levels, Gold);
        CastingLotsDao.log.info("First place match {} of tournament {} in category {}",
                firstPlaceMid, tid, cid);
        generateTree(levels, firstPlaceMid, tid, cid, lowestPriority - 1);
        return firstPlaceMid;
    }

    public void generateTree(int level, int parentMid, int tid, int cid, int priority) {
        if (level <= 0) {
            return;
        }
        int mid = matchDao.createPlayOffMatch(tid, cid, of(parentMid), empty(), priority, level, POff);
        generateTree(level - 1, mid, tid, cid, priority - 1);
    }
}
