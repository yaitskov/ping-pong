package org.dan.ping.pong.app.score;

import static java.util.Collections.singletonList;
import static ord.dan.ping.pong.jooq.Tables.MATCHES;
import static ord.dan.ping.pong.jooq.Tables.SET_SCORE;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.tournament.Tid;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

@Slf4j
public class MatchScoreDao {
    @Inject
    private DSLContext jooq;

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
