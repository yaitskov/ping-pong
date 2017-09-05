package org.dan.ping.pong.app.bid;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import org.dan.ping.pong.app.tournament.DbUpdater;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class BidService {
    public static final List<BidState> WIN_STATES = asList(Win1, Win2, Win3);

    @Inject
    private BidDao bidDao;

    @Transactional(TRANSACTION_MANAGER)
    public void paid(BidId bidId) {
        bidDao.setBidState(bidId, singletonList(Want), Paid, clocker.get());
    }

    @Transactional(TRANSACTION_MANAGER)
    public void readyToPlay(BidId bidId) {
        bidDao.setBidState(bidId, asList(Paid, Want), Here, clocker.get());
    }

    public List<ParticipantState> findEnlisted(int tid) {
        return bidDao.findEnlisted(tid);
    }

    public DatedParticipantState getParticipantState(int tid, int uid) {
        return bidDao.getParticipantInfo(tid, uid)
                .orElseThrow(() -> notFound("Participant has not been found"));
    }

    @Transactional(TRANSACTION_MANAGER)
    public void disappeared(BidId bidId) {
        bidDao.setBidState(bidId, asList(Here, Paid), Want, clocker.get());
    }

    @Inject
    private Clocker clocker;

    @Transactional(TRANSACTION_MANAGER)
    public void setCategory(SetCategory setCategory) {
        bidDao.setCategory(setCategory, clocker.get());
    }

    @Transactional(TRANSACTION_MANAGER)
    public void setBidState(SetBidState setState) {
        bidDao.setBidState(setState.getTid(), setState.getUid(),
                setState.getExpected(), setState.getTarget(), clocker.get());
    }

    public void setBidState(ParticipantMemState bid, BidState target,
            List<BidState> expected, DbUpdater batch) {
        bid.setBidState(target);
        bidDao.setBidState(bid.getTid().getTid(), bid.getUid().getId(),
                target, expected, clocker.get(), batch);
    }
}
