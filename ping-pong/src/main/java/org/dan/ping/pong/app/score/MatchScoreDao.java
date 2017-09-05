package org.dan.ping.pong.app.score;

import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.tables.MatchScore.MATCH_SCORE;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.sys.error.PiPoEx;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class MatchScoreDao {
    @Inject
    private DSLContext jooq;

    public void createScore(int mid, int uid, int cid, int tid) {
        createScore(mid, uid, cid, tid, 0, 0);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void createScore(int mid, int uid, int cid, int tid, int won, int score) {
        log.info("create score for min {} uid {}", mid, uid);
        jooq.insertInto(MATCH_SCORE, MATCH_SCORE.MID, MATCH_SCORE.UID,
                MATCH_SCORE.CID, MATCH_SCORE.TID,
                MATCH_SCORE.WON, MATCH_SCORE.SETS_WON)
                .values(mid, uid, cid, tid, won, score)
                .execute();
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<ScoreInfo> get(int mid, int uid) {
        return ofNullable(jooq
                .select(MATCH_SCORE.SETS_WON, MATCH_SCORE.WON, MATCH_SCORE.TID)
                .from(MATCH_SCORE)
                .where(MATCH_SCORE.MID.eq(mid), MATCH_SCORE.UID.eq(uid))
                .fetchOne())
                .map(r -> ScoreInfo.builder()
                        .score(r.get(MATCH_SCORE.SETS_WON))
                        .tid(r.get(MATCH_SCORE.TID))
                        .won(r.get(MATCH_SCORE.WON))
                        .uid(uid)
                        .build());
    }

}
