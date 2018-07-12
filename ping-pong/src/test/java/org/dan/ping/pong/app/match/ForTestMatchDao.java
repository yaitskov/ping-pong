package org.dan.ping.pong.app.match;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.jooq.Tables.MATCHES;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.tournament.Tid;
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
    public List<Mid> findIncompleteTournamentMatches(Tid tid) {
        return jooq.select(MATCHES.MID).from(MATCHES)
                .where(MATCHES.TID.eq(tid), MATCHES.STATE.ne(MatchState.Over))
                .fetch()
                .map(Record1::value1);
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<MatchInfo> getById(Tid tid, Mid mid) {
        return ofNullable(jooq.select(MATCHES.TID, MATCHES.STATE, MATCHES.GID,
                MATCHES.CID, MATCHES.LOSE_MID, MATCHES.WIN_MID, MATCHES.TYPE,
                MATCHES.LEVEL, MATCHES.PRIORITY)
                .from(MATCHES)
                .where(MATCHES.TID.eq(tid), MATCHES.MID.eq(mid))
                .fetchOne()).map(r ->
                MatchInfo.builder()
                        .gid(r.get(MATCHES.GID).map(Gid::new))
                        .mid(mid)
                        .cid(r.get(MATCHES.CID))
                        .type(r.get(MATCHES.TYPE))
                        .state(r.get(MATCHES.STATE))
                        .tid(r.get(MATCHES.TID))
                        .level(ofNullable(r.get(MATCHES.LEVEL)).orElse(0))
                        .priority(r.get(MATCHES.PRIORITY))
                        .winnerMid(r.get(MATCHES.WIN_MID))
                        .loserMid(r.get(MATCHES.LOSE_MID))
                        .build());
    }
}
