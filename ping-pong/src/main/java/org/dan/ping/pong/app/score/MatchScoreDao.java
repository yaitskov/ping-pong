package org.dan.ping.pong.app.score;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static ord.dan.ping.pong.jooq.Tables.MATCHES;
import static ord.dan.ping.pong.jooq.Tables.SET_SCORE;
import static ord.dan.ping.pong.jooq.tables.MatchScore.MATCH_SCORE;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import com.google.common.collect.ImmutableMap;
import com.sun.glass.ui.delegate.MenuItemDelegate;
import lombok.extern.slf4j.Slf4j;
import ord.dan.ping.pong.jooq.Tables;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.error.PiPoEx;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectJoinStep;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public Map<Integer, Map<Integer, List<Integer>>> load(Tid tid) {
        final Map<Integer, Map<Integer, List<Integer>>> result = new HashMap<>();
        jooq.select(SET_SCORE.MID, SET_SCORE.UID, SET_SCORE.GAMES)
                .from(SET_SCORE)
                .innerJoin(MATCHES)
                .on(SET_SCORE.MID.eq(MATCHES.MID))
                .where(MATCHES.TID.eq(tid.getTid()))
                .orderBy(SET_SCORE.SET_ID)
                .fetch()
                .forEach(r -> {
                    final int mid = r.get(SET_SCORE.MID);
                    final int uid = r.get(SET_SCORE.UID);
                    final int games = r.get(SET_SCORE.GAMES);
                    Map<Integer, List<Integer>> scores = result.get(mid);
                    if (scores == null) {
                        scores = new HashMap<>();
                        scores.put(uid, new ArrayList<>(singletonList(games)));
                        result.put(mid, scores);
                    } else {
                        List<Integer> sets = scores.get(uid);
                        if (sets == null) {
                            scores.put(uid, new ArrayList<>(singletonList(games)));
                        } else {
                            sets.add(games);
                        }
                    }
                });
        return result;
    }


}
