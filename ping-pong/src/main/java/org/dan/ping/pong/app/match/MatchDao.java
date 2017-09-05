package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.BID;
import static ord.dan.ping.pong.jooq.Tables.CATEGORY;
import static ord.dan.ping.pong.jooq.Tables.SET_SCORE;
import static ord.dan.ping.pong.jooq.Tables.TABLES;
import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT;
import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT_ADMIN;
import static ord.dan.ping.pong.jooq.Tables.USERS;
import static ord.dan.ping.pong.jooq.tables.Matches.MATCHES;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.app.match.MatchState.Draft;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.match.MatchType.Grup;
import static org.dan.ping.pong.app.tournament.DbUpdate.JUST_A_ROW;
import static org.dan.ping.pong.app.tournament.DbUpdate.NON_ZERO_ROWS;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import ord.dan.ping.pong.jooq.tables.Bid;
import ord.dan.ping.pong.jooq.tables.Users;
import org.dan.ping.pong.app.category.CategoryInfo;
import org.dan.ping.pong.app.table.TableLink;
import org.dan.ping.pong.app.tournament.DbUpdate;
import org.dan.ping.pong.app.tournament.DbUpdater;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.PlayOffMatchForResign;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.user.UserLink;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class MatchDao {
    private static final Users ENEMY_USER = USERS.as("enemy_user");
    public static final int FIRST_PLAY_OFF_MATCH_LEVEL = 1;
    private static final Bid BID_2 = BID.as("bid2");
    private static final Field<Integer> UID_2 = BID_2.UID.as("uid2");
    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public int createGroupMatch(int tid, int gid, int cid, int priorityGroup) {
        log.info("Create a match in group {} of tournament {}", gid, tid);
        return jooq.insertInto(MATCHES, MATCHES.TID,
                MATCHES.GID, MATCHES.CID,
                MATCHES.STATE, MATCHES.PRIORITY, MATCHES.TYPE)
                .values(tid, Optional.of(gid), cid, Place, priorityGroup, Grup)
                .returning()
                .fetchOne()
                .getMid();
    }

    @Transactional(TRANSACTION_MANAGER)
    public int createPlayOffMatch(int tid, Integer cid,
            Optional<Integer> winMid, Optional<Integer> loseMid,
            int priority, int level, MatchType type) {
        return jooq.insertInto(MATCHES, MATCHES.TID, MATCHES.CID,
                MATCHES.PRIORITY, MATCHES.STATE,
                MATCHES.WIN_MID, MATCHES.LOSE_MID, MATCHES.LEVEL, MATCHES.TYPE)
                .values(tid, cid, priority, Draft, winMid, loseMid, level, type)
                .returning()
                .fetchOne()
                .getMid();
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<OpenMatchForJudge> findOpenMatchesFurJudge(int adminUid) {
        return jooq.select(MATCHES.MID, MATCHES.TID, MATCHES.STARTED,
                TABLES.TABLE_ID, TABLES.LABEL, TOURNAMENT.MATCH_SCORE,
                MATCHES.TYPE, USERS.UID, USERS.NAME,
                ENEMY_USER.UID, ENEMY_USER.NAME)
                .from(TOURNAMENT_ADMIN)
                .innerJoin(TOURNAMENT)
                .on(TOURNAMENT_ADMIN.TID.eq(TOURNAMENT.TID))
                .innerJoin(MATCHES)
                .on(TOURNAMENT.TID.eq(MATCHES.TID))
                .innerJoin(MATCH_SCORE)
                .on(MATCH_SCORE.MID.eq(MATCHES.MID))
                .innerJoin(ENEMY_SCORE)
                .on(ENEMY_SCORE.MID.eq(MATCHES.MID))
                .innerJoin(USERS)
                .on(MATCH_SCORE.UID.eq(USERS.UID))
                .innerJoin(ENEMY_USER)
                .on(ENEMY_SCORE.UID.eq(ENEMY_USER.UID))
                .innerJoin(TABLES)
                .on(MATCHES.MID.eq(TABLES.MID.cast(Integer.class)))
                .where(TOURNAMENT_ADMIN.UID.eq(adminUid),
                        ENEMY_SCORE.UID.lt(MATCH_SCORE.UID),
                        MATCHES.STATE.eq(Game))
                .orderBy(MATCHES.STARTED)
                .fetch()
                .map(r -> OpenMatchForJudge.builder()
                        .mid(r.get(MATCHES.MID))
                        .tid(r.get(MATCHES.TID))
                        .matchScore(r.get(TOURNAMENT.MATCH_SCORE))
                        .started(r.get(MATCHES.STARTED).get())
                        .type(r.get(MATCHES.TYPE))
                        .table(TableLink.builder()
                                .id(r.get(TABLES.TABLE_ID))
                                .label(r.get(TABLES.LABEL))
                                .build())
                        .participants(asList(
                                UserLink.builder()
                                        .name(r.get(USERS.NAME))
                                        .uid(r.get(USERS.UID))
                                        .build(),
                                UserLink.builder()
                                        .name(r.get(ENEMY_USER.NAME))
                                        .uid(r.get(ENEMY_USER.UID))
                                        .build()))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<MyPendingMatch> findPendingMatches(int uid) {
        return jooq.select(MATCHES.MID, MATCHES.TID, TABLES.TABLE_ID,
                TABLES.LABEL, MATCHES.TYPE, ENEMY_USER.UID, ENEMY_USER.NAME,
                MATCHES.STATE, TOURNAMENT.MATCH_SCORE)
                .from(MATCH_SCORE)
                .innerJoin(TOURNAMENT).on(TOURNAMENT.TID.eq(MATCH_SCORE.TID))
                .innerJoin(MATCHES)
                .on(MATCH_SCORE.MID.eq(MATCHES.MID))
                .leftJoin(ENEMY_SCORE)
                .on(ENEMY_SCORE.MID.eq(MATCHES.MID))
                .innerJoin(USERS)
                .on(MATCH_SCORE.UID.eq(USERS.UID))
                .leftJoin(ENEMY_USER)
                .on(ENEMY_SCORE.UID.eq(ENEMY_USER.UID))
                .leftJoin(TABLES)
                .on(MATCHES.MID.eq(TABLES.TABLE_ID))
                .where(MATCH_SCORE.UID.eq(uid),
                        ENEMY_SCORE.UID.notEqual(uid),
                        MATCHES.STATE.in(Place, Game))
                .orderBy(MATCHES.STATE.asc(), MATCHES.MID.asc())
                .fetch()

                .map(r -> MyPendingMatch.builder()
                        .tid(r.get(MATCHES.TID))
                        .mid(r.get(MATCHES.MID))
                        .matchScore(r.get(TOURNAMENT.MATCH_SCORE))
                        .state(r.get(MATCHES.STATE))
                        .matchType(r.get(MATCHES.TYPE))
                        .table(ofNullable(r.get(TABLES.TABLE_ID))
                                .map(tableId -> TableLink.builder()
                                        .id(tableId)
                                        .label(r.get(TABLES.LABEL))
                                        .build()))
                        .enemy(ofNullable(r.get(ENEMY_USER.UID))
                                .map(enemyId -> UserLink.builder()
                                        .name(r.get(ENEMY_USER.NAME))
                                        .uid(enemyId)
                                        .build()))
                        .build());
    }

    public void changeStatus(int mid, MatchState state, DbUpdater batch) {
        batch.exec(DbUpdate.builder()
                .mustAffectRows(NON_ZERO_ROWS)
                .logBefore(() -> log.info("Put match {} into {}", mid, state))
                .query(jooq.update(MATCHES)
                        .set(MATCHES.STATE, state)
                        .where(MATCHES.MID.eq(mid)))
                .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<MatchInfo> getById(int mid) {
        final Map<Integer, Integer> participantIdScore = new HashMap<>();
        final MatchInfo[] result = new MatchInfo[1];
        jooq.select(MATCHES.TID, MATCHES.STATE, MATCHES.GID,
                MATCHES.CID, MATCHES.LOSE_MID, MATCHES.WIN_MID,
                MATCH_SCORE.UID, MATCH_SCORE.SETS_WON, MATCHES.TYPE)
                .from(MATCHES)
                .leftJoin(MATCH_SCORE)
                .on(MATCHES.MID.eq(MATCH_SCORE.MID))
                .where(MATCHES.MID.eq(mid))
                .fetch().forEach(r -> {
            participantIdScore.put(r.get(MATCH_SCORE.UID), r.get(MATCH_SCORE.SETS_WON));
            if (result[0] == null) {
                result[0] = MatchInfo.builder()
                        .gid(r.get(MATCHES.GID))
                        .mid(mid)
                        .cid(r.get(MATCHES.CID))
                        .type(r.get(MATCHES.TYPE))
                        .state(r.get(MATCHES.STATE))
                        .tid(r.get(MATCHES.TID))
                        .winnerMid(r.get(MATCHES.WIN_MID))
                        .loserMid(r.get(MATCHES.LOSE_MID))
                        //.participantIdScore(participantIdScore)
                        .build();
            }
        });
        return ofNullable(result[0]);
    }

    public Optional<Integer> scoreSet(OpenTournamentMemState tournament, MatchInfo matchInfo,
            DbUpdater batch, FinalMatchScore matchScore) {
        final Optional<Integer> winUidO = matchInfo.addSetScore(
                matchScore.getScores(), tournament.getRule().getMatch());
        insertSetScore(batch, matchScore);
        return winUidO;
    }

    public void completeMatch(int mid, int winUid, Instant now, DbUpdater batch) {
        batch.exec(DbUpdate.builder()
                .mustAffectRows(JUST_A_ROW)
                .query(jooq.update(MATCHES)
                        .set(MATCHES.STATE, MatchState.Over)
                        .set(MATCHES.WIN_MID, Optional.of(winUid))
                        .set(MATCHES.ENDED, Optional.of(now))
                        .where(MATCHES.MID.eq(mid),
                                MATCHES.STATE.eq(Game)))
                .build());
    }

    private void insertSetScore(DbUpdater batch, FinalMatchScore matchScore) {
        matchScore.getScores().forEach(score -> batch.exec(
                jooq.insertInto(SET_SCORE, SET_SCORE.MID,
                        SET_SCORE.UID, SET_SCORE.GAMES)
                        .values(matchScore.getMid(), score.getUid(), score.getScore())));
    }

    @Transactional(TRANSACTION_MANAGER)
    public void markAsSchedule(MatchInfo mid, Instant now) {
        if (0 == jooq.update(MATCHES)
                .set(MATCHES.STATE, Game)
                .set(MATCHES.STARTED, Optional.of(now))
                .where(MATCHES.MID.eq(mid),
                        MATCHES.STATE.eq(Place))
                .execute()) {
            throw internalError("Match "
                    + mid + " is not in place state");
        }
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<CompleteMatch> findCompleteMatches(Integer tid) {
        return jooq
                .select(MATCHES.MID, MATCHES.STARTED, MATCHES.TYPE,
                        MATCHES.ENDED, ENEMY_USER.UID,
                        ENEMY_USER.NAME, USERS.UID, USERS.NAME)
                .from(TOURNAMENT)
                .innerJoin(MATCHES)
                .on(TOURNAMENT.TID.eq(MATCHES.TID))
                .innerJoin(USERS)
                .on(MATCHES.UID_LESS.eq(USERS.UID))
                .innerJoin(ENEMY_USER)
                .on(MATCHES.UID_MORE.eq(ENEMY_USER.UID))
                .where(TOURNAMENT.TID.eq(tid),
                        MATCHES.STATE.eq(Over))
                .orderBy(MATCHES.STARTED)
                .fetch()
                .map(r -> CompleteMatch.builder()
                        .mid(r.get(MATCHES.MID))
                        .started(r.get(MATCHES.STARTED).get())
                        .ended(r.get(MATCHES.ENDED).get())
                        .type(r.get(MATCHES.TYPE))
                        .participants(asList(
                                UserLink.builder()
                                        .name(r.get(USERS.NAME))
                                        .uid(r.get(USERS.UID))
                                        .build(),
                                UserLink.builder()
                                        .name(r.get(ENEMY_USER.NAME))
                                        .uid(r.get(ENEMY_USER.UID))
                                        .build()))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<OpenMatchForWatch> findOpenMatchesForWatching(int tid) {
        return jooq
                .select(MATCHES.MID, MATCHES.STARTED, CATEGORY.NAME,
                        MATCHES.CID, TABLES.TABLE_ID, TABLES.LABEL,
                        MATCHES.TYPE, ENEMY_USER.UID, ENEMY_USER.NAME,
                        MATCH_SCORE.SETS_WON, ENEMY_SCORE.SETS_WON,
                        USERS.UID, USERS.NAME)
                .from(MATCHES)
                .innerJoin(MATCH_SCORE)
                .on(MATCH_SCORE.MID.eq(MATCHES.MID))
                .innerJoin(ENEMY_SCORE)
                .on(ENEMY_SCORE.MID.eq(MATCHES.MID))
                .innerJoin(USERS)
                .on(MATCH_SCORE.UID.eq(USERS.UID))
                .innerJoin(ENEMY_USER)
                .on(ENEMY_SCORE.UID.eq(ENEMY_USER.UID))
                .innerJoin(CATEGORY)
                .on(MATCHES.CID.eq(CATEGORY.CID))
                .innerJoin(TABLES)
                .on(MATCHES.MID.eq(TABLES.MID.cast(Integer.class)))
                .where(MATCHES.TID.eq(tid),
                        MATCHES.STATE.eq(Game),
                        ENEMY_SCORE.UID.lt(MATCH_SCORE.UID))
                .orderBy(MATCHES.STARTED)
                .fetch()
                .map(r -> OpenMatchForWatch.builder()
                        .mid(r.get(MATCHES.MID))
                        .started(r.get(MATCHES.STARTED).get())
                        .score(asList(r.get(MATCH_SCORE.SETS_WON), r.get(ENEMY_SCORE.SETS_WON)))
                        .category(CategoryInfo.builder()
                                .cid(r.get(MATCHES.CID))
                                .name(r.get(CATEGORY.NAME))
                                .build())
                        .table(TableLink.builder()
                                .id(r.get(TABLES.TABLE_ID))
                                .label(r.get(TABLES.LABEL))
                                .build())
                        .type(r.get(MATCHES.TYPE))
                        .participants(asList(
                                UserLink.builder()
                                        .name(r.get(USERS.NAME))
                                        .uid(r.get(USERS.UID))
                                        .build(),
                                UserLink.builder()
                                        .name(r.get(ENEMY_USER.NAME))
                                        .uid(r.get(ENEMY_USER.UID))
                                        .build()))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<UserLink> findWinners(int tid) {
        return jooq.select(USERS.NAME, BID.UID)
                .from(BID)
                .innerJoin(USERS).on(BID.UID.eq(USERS.UID))
                .where(BID.TID.eq(tid), BID.STATE.in(Win1, Win2, Win3))
                .orderBy(BID.STATE.asc())
                .fetch()
                .map(r -> UserLink
                        .builder()
                        .name(r.get(USERS.NAME))
                        .uid(r.get(BID.UID))
                        .build());
    }

    @Transactional(TRANSACTION_MANAGER)
    public void deleteAllByTid(int tid) {
        jooq.deleteFrom(SET_SCORE)
                .where(SET_SCORE.TID.eq(tid))
                .execute();
        jooq.deleteFrom(MATCHES)
                .where(MATCHES.TID.eq(tid))
                .execute();
    }

    @Transactional(TRANSACTION_MANAGER)
    public void scoreSet(int uid, GroupMatchForResign match, Instant now) {
        List<Integer> result = Ints.asList(jooq.batch(
                    jooq.update(MATCHES)
                            .set(MATCHES.STATE, MatchState.Over)
                            .set(MATCHES.ENDED, Optional.of(now))
                            .where(MATCHES.MID.eq(match.getMid()),
                                    MATCHES.STATE.in(Game, Place)),
                    jooq.update(MATCH_SCORE)
                            .set(MATCH_SCORE.UPDATED, Optional.of(now))
                            .set(MATCH_SCORE.WON, -1)
                            .set(MATCH_SCORE.SETS_WON, 0)
                            .where(MATCH_SCORE.UID.eq(uid),
                                    MATCH_SCORE.MID.eq(match.getMid())),
                    jooq.update(MATCH_SCORE)
                            .set(MATCH_SCORE.UPDATED, Optional.of(now))
                            .set(MATCH_SCORE.WON, 1)
                            .set(MATCH_SCORE.SETS_WON, 0)
                            .where(MATCH_SCORE.UID.eq(match.getOpponentUid()),
                                    MATCH_SCORE.MID.eq(match.getMid())))
                    .execute());
        if (match.getState() == Draft) {
            if (!result.equals(asList(1, 1, 0))) {
                throw internalError("Wrong update pattern " + result);
            }
        } else if (match.getState() == Game || match.getState() == Place) {
            if (!result.equals(asList(1, 1, 1))) {
                throw internalError("Wrong update pattern " + result);
            }
        } else {
            throw internalError("Unexpected");
        }
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<GroupMatchForResign> groupMatchesOfParticipant(int uid, int tid) {
        return jooq
                .select(MATCHES.MID, MATCHES.STATE, MATCHES.GID, ENEMY_SCORE.UID)
                .from(MATCH_SCORE)
                .innerJoin(MATCHES).on(MATCH_SCORE.MID.eq(MATCHES.MID))
                .innerJoin(ENEMY_SCORE).on(ENEMY_SCORE.MID.eq(MATCHES.MID))
                .where(MATCH_SCORE.TID.eq(tid), MATCH_SCORE.UID.eq(uid),
                        ENEMY_SCORE.UID.ne(uid), MATCHES.GID.isNotNull())
                .fetch()
                .map(r -> GroupMatchForResign
                        .builder()
                        .mid(r.get(MATCHES.MID))
                        .gid(r.get(MATCHES.GID).get())
                        .opponentUid(r.get(ENEMY_SCORE.UID))
                        .state(r.get(MATCHES.STATE))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<PlayOffMatchForResign> playOffMatchForResign(int uid, int tid) {
        return ofNullable(jooq
                .select(MATCHES.STATE, MATCHES.MID, MATCHES.WIN_MID,
                        MATCHES.LOSE_MID, ENEMY_SCORE.UID, MATCHES.TYPE)
                .from(MATCH_SCORE)
                .innerJoin(MATCHES).on(MATCH_SCORE.MID.eq(MATCHES.MID))
                .leftJoin(ENEMY_SCORE).on(
                        MATCHES.MID.eq(ENEMY_SCORE.MID),
                        ENEMY_SCORE.UID.ne(uid))
                .where(MATCH_SCORE.UID.eq(uid), MATCH_SCORE.TID.eq(tid),
                        MATCHES.STATE.in(Game, Place, Draft))
                .fetchOne())
                .map(r -> PlayOffMatchForResign.builder()
                        .winMatch(r.get(MATCHES.WIN_MID))
                        .lostMatch(r.get(MATCHES.LOSE_MID))
                        .mid(r.get(MATCHES.MID))
                        .state(r.get(MATCHES.STATE))
                        .type(r.get(MATCHES.TYPE))
                        .opponentId(ofNullable(r.get(ENEMY_SCORE.UID)))
                        .build());
    }

    public void setParticipant(int n, int mid, int uid, DbUpdater batch) {
        batch.exec(DbUpdate.builder()
                .mustAffectRows(NON_ZERO_ROWS)
                .query(jooq.update(MATCHES)
                        .set(n == 0
                                ? MATCHES.UID_LESS
                                : MATCHES.UID_MORE, uid)
                        .where(MATCHES.MID.eq(mid)))
                .build());
    }

    public List<MatchInfo> load(Tid tid) {
        return jooq.select(MATCHES.MID, MATCHES.GID, MATCHES.CID,
                MATCHES.WIN_MID, MATCHES.LOSE_MID,
                MATCHES.STATE, MATCHES.TYPE, MATCHES.ENDED,
                MATCHES.UID_LESS, MATCHES.UID_MORE, MATCHES.UID_WIN)
                .from(MATCHES)
                .where(MATCHES.TID.eq(tid.getTid()))
                .fetch()
                .map(r -> MatchInfo.builder()
                        .mid(r.get(MATCHES.MID))
                        .cid(r.get(MATCHES.CID))
                        .gid(r.get(MATCHES.GID))
                        .state(r.get(MATCHES.STATE))
                        .type(r.get(MATCHES.TYPE))
                        .loserMid(r.get(MATCHES.LOSE_MID))
                        .winnerMid(r.get(MATCHES.WIN_MID))
                        .winnerId(r.get(MATCHES.UID_WIN))
                        .tid(tid.getTid())
                        .build());
    }

}
