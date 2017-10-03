package org.dan.ping.pong.app.match;

import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.MATCHES;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import ord.dan.ping.pong.jooq.tables.Matches;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<MatchInfo> getById(int mid) {
        return ofNullable(jooq.select(Matches.MATCHES.TID, Matches.MATCHES.STATE, Matches.MATCHES.GID,
                Matches.MATCHES.CID, Matches.MATCHES.LOSE_MID, Matches.MATCHES.WIN_MID, Matches.MATCHES.TYPE)
                .from(Matches.MATCHES)
                .where(Matches.MATCHES.MID.eq(mid))
                .fetchOne()).map(r ->
                MatchInfo.builder()
                        .gid(r.get(Matches.MATCHES.GID))
                        .mid(mid)
                        .cid(r.get(Matches.MATCHES.CID))
                        .type(r.get(Matches.MATCHES.TYPE))
                        .state(r.get(Matches.MATCHES.STATE))
                        .tid(r.get(Matches.MATCHES.TID))
                        .winnerMid(r.get(Matches.MATCHES.WIN_MID))
                        .loserMid(r.get(Matches.MATCHES.LOSE_MID))
                        .build());
    }
}
