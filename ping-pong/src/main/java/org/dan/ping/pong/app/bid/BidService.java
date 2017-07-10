package org.dan.ping.pong.app.bid;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

public class BidService {
    @Inject
    private BidDao bidDao;

    @Transactional(TRANSACTION_MANAGER)
    public void paid(BidId bidId) {
        bidDao.setBidState(bidId, singletonList(Want), Paid);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void readyToPlay(BidId bidId) {
        bidDao.setBidState(bidId, asList(Paid, Want), Here);
    }

    public List<ParticipantState> findEnlisted(int tid) {
        return bidDao.findEnlisted(tid);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void disappeared(BidId bidId) {
        bidDao.setBidState(bidId, asList(Here, Paid), Want);
    }
}
