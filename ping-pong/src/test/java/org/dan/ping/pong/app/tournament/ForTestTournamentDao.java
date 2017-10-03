package org.dan.ping.pong.app.tournament;

import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

public class ForTestTournamentDao {
    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public void setState(int tid, TournamentState state) {
        jooq.update(TOURNAMENT)
                .set(TOURNAMENT.STATE, state)
                .where(TOURNAMENT.TID.eq(tid))
                .execute();
    }
}
