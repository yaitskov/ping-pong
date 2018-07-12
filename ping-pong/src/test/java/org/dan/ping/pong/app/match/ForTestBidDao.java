package org.dan.ping.pong.app.match;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.jooq.tables.Bid.BID;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.jooq.Tables;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

public class ForTestBidDao {
    @Inject
    private DSLContext jooq;

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<Bid> findByTidAndState(Tid tid, Cid cid, List<BidState> state) {
        return jooq.select(BID.BID_)
                .from(BID)
                .where(BID.TID.eq(tid), BID.CID.eq(cid), BID.STATE.in(state))
                .orderBy(BID.STATE)
                .fetch()
                .map(Record1::value1);
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<BidState> getState(Tid tid, Bid bid) {
        return ofNullable(
                jooq.select(Tables.BID.STATE)
                        .from(Tables.BID)
                        .where(Tables.BID.TID.eq(tid), Tables.BID.BID_.eq(bid))
                        .fetchOne())
                .map(Record1::value1);
    }
}
