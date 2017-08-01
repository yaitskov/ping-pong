package org.dan.ping.pong.app.match;

import static ord.dan.ping.pong.jooq.tables.Bid.BID;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.dan.ping.pong.app.bid.BidState;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

public class ForTestBidDao {
    @Inject
    private DSLContext jooq;

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<Integer> findByTidAndState(int tid, int cid, List<BidState> state) {
        return jooq.select(BID.UID)
                .from(BID)
                .where(BID.TID.eq(tid), BID.CID.eq(cid), BID.STATE.in(state))
                .orderBy(BID.STATE)
                .fetch()
                .map(Record1::value1);
    }
}
