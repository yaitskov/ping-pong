package org.dan.ping.pong.app.castinglots;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.log;
import static java.util.Optional.empty;
import static ord.dan.ping.pong.jooq.Tables.BID;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.match.MatchType.Brnz;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.match.MatchType.POff;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.TournamentBid;
import org.dan.ping.pong.app.bid.TournamentGroupingBid;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.score.MatchScoreDao;
import org.dan.ping.pong.app.tournament.TournamentInfo;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class CastingLotsDao {
    @Inject
    private DSLContext jooq;

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<TournamentGroupingBid> findBidsReadyToPlay(int tid) {
        return jooq.select(BID.UID, BID.CID, BID.STATE).from(BID)
                .where(BID.TID.eq(tid), BID.STATE.in(Want, Paid, Here))
                .orderBy(BID.UID)
                .fetch()
                .map(r -> TournamentGroupingBid
                        .builder()
                        .uid(r.get(BID.UID))
                        .state(r.get(BID.STATE))
                        .cid(r.get(BID.CID))
                        .build());
    }

    @Inject
    private MatchDao matchDao;

    @Inject
    private MatchScoreDao matchScoreDao;

    @Transactional(TRANSACTION_MANAGER)
    public int generateGroupMatches(int gid, List<TournamentGroupingBid> groupBids, int tid) {
        CastingLotsDao.log.info("Generate matches for group {} in tournament {}", gid, tid);
        int priorityGroup = 0;
        for (int i = 0; i < groupBids.size(); ++i) {
            final TournamentGroupingBid bid1 = groupBids.get(i);
            for (int j = i + 1; j < groupBids.size(); ++j) {
                final TournamentGroupingBid bid2 = groupBids.get(j);
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
    public int generatePlayOffMatches(TournamentInfo tinfo, Integer cid,
            int playOffStartPositions, int basePlayOffPriority) {
        final int tid = tinfo.getTid();
        CastingLotsDao.log.info("Generate play off matches for {} groups in tournament {}",
                playOffStartPositions, tid);
        if (playOffStartPositions == 1) {
            CastingLotsDao.log.info("Tournament {}:{} will be without play off", tid, cid);
            return 0;
        } else {
            checkArgument(playOffStartPositions > 0, "not enough groups %s", playOffStartPositions);
            checkArgument(playOffStartPositions % 2 == 0, "odd number groups %s", playOffStartPositions);
        }
        final int levels = (int) (log(playOffStartPositions) / log(2));
        final int lowestPriority = basePlayOffPriority + levels;
        return PlayOffGenerator.builder()
                .tid(tid)
                .cid(cid)
                .thirdPlaceMatch(tinfo.getThirdPlaceMath() > 0)
                .matchDao(matchDao)
                .build()
                .generateTree(levels, empty(), lowestPriority,
                        TypeChain.of(Gold, POff), empty()).get();
    }
}
