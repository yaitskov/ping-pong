package org.dan.ping.pong.app.match;

import static ord.dan.ping.pong.jooq.Tables.MATCHES;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

public class ForTestMatchDao {
    @Inject
    private DSLContext jooq;

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<Integer> findIncompleteTournamentMatches(int tid) {
        return jooq.select(MATCHES.MID).from(MATCHES)
                .where(MATCHES.TID.eq(tid), MATCHES.STATE.ne(MatchState.Over))
                .fetch()
                .map(Record1::value1);
    }
}
