package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.MATCHES;
import static ord.dan.ping.pong.jooq.Tables.MATCH_SCORE;
import static org.dan.ping.pong.app.match.MatchDao.ENEMY_SCORE;
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

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public TestMatchInfo findByParticipants(int tid, int uid1, int uid2) {
        return ofNullable(
                jooq.select(MATCHES.MID, MATCHES.STATE,
                        MATCH_SCORE.UID, ENEMY_SCORE.UID,
                        MATCH_SCORE.SETS_WON, MATCH_SCORE.WON,
                        ENEMY_SCORE.SETS_WON, ENEMY_SCORE.WON)
                        .from(MATCHES)
                        .innerJoin(MATCH_SCORE).on(MATCHES.MID.eq(MATCH_SCORE.MID))
                        .innerJoin(ENEMY_SCORE).on(MATCHES.MID.eq(ENEMY_SCORE.MID))
                        .where(MATCHES.TID.eq(tid), MATCH_SCORE.UID.eq(uid1),
                                ENEMY_SCORE.UID.eq(uid2))
                        .fetchOne())
                .map(r -> TestMatchInfo.builder()
                        .mid(r.get(MATCHES.MID))
                        .state(r.get(MATCHES.STATE))
                        .scores(asList(
                                ParticipantScoreInfo.builder()
                                        .uid(r.get(MATCH_SCORE.UID))
                                        .score(r.get(MATCH_SCORE.SETS_WON))
                                        .won(r.get(MATCH_SCORE.WON))
                                        .build(),
                                ParticipantScoreInfo.builder()
                                        .uid(r.get(ENEMY_SCORE.UID))
                                        .score(r.get(ENEMY_SCORE.SETS_WON))
                                        .won(r.get(ENEMY_SCORE.WON))
                                        .build()))
                        .build())
                .orElseThrow(() -> new IllegalArgumentException("No match in tid "
                        + tid + " between players " + uid1 + " " + uid2));
    }
}
