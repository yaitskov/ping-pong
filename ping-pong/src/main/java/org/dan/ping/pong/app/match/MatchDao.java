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
    public int createGroupMatch(int tid, int gid, int cid, int priorityGroup, int uid1, int uid2) {
        log.info("Create a match in group {} of tournament {}", gid, tid);
        return jooq.insertInto(MATCHES, MATCHES.TID,
                MATCHES.GID, MATCHES.CID,
                MATCHES.STATE, MATCHES.PRIORITY, MATCHES.TYPE,
                MATCHES.UID_LESS, MATCHES.UID_MORE)
                .values(tid, Optional.of(gid), cid, Place, priorityGroup, Grup, uid1, uid2)
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
                .innerJoin(USERS)
                .on(MATCHES.UID_LESS.eq(USERS.UID))
                .innerJoin(ENEMY_USER)
                .on(MATCHES.UID_MORE.eq(ENEMY_USER.UID))
                .innerJoin(TABLES)
                .on(MATCHES.MID.eq(TABLES.MID.cast(Integer.class)))
                .where(TOURNAMENT_ADMIN.UID.eq(adminUid),
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
                .from(MATCHES)
                .innerJoin(TOURNAMENT)
                .on(TOURNAMENT.TID.eq(MATCHES.TID))
                .innerJoin(USERS)
                .on(MATCHES.UID_LESS.eq(USERS.UID))
                .leftJoin(ENEMY_USER)
                .on(MATCHES.UID_MORE.eq(ENEMY_USER.UID))
                .leftJoin(TABLES)
                .on(MATCHES.MID.eq(TABLES.TABLE_ID))
                .where(MATCHES.UID_MORE.eq(uid).or(MATCHES.UID_LESS.eq(uid)),
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

    public Optional<Integer> scoreSet(OpenTournamentMemState tournament, MatchInfo matchInfo,
            DbUpdater batch, FinalMatchScore matchScore) {
        final Optional<Integer> winUidO = matchInfo.addSetScore(
                matchScore.getScores(), tournament.getRule().getMatch());
        insertSetScore(batch, matchScore);
        return winUidO;
    }

    public void completeMatch(int mid, int winUid, Instant now, DbUpdater batch, MatchState expected) {
        batch.exec(DbUpdate.builder()
                .mustAffectRows(JUST_A_ROW)
                .query(jooq.update(MATCHES)
                        .set(MATCHES.STATE, MatchState.Over)
                        .set(MATCHES.WIN_MID, Optional.of(winUid))
                        .set(MATCHES.ENDED, Optional.of(now))
                        .where(MATCHES.MID.eq(mid),
                                MATCHES.STATE.eq(expected)))
                .build());
    }

    private void insertSetScore(DbUpdater batch, FinalMatchScore matchScore) {
        matchScore.getScores().forEach(score -> batch.exec(
                jooq.insertInto(SET_SCORE, SET_SCORE.MID,
                        SET_SCORE.UID, SET_SCORE.GAMES)
                        .values(matchScore.getMid(), score.getUid(), score.getScore())));
    }

    @Transactional(TRANSACTION_MANAGER)
    public void markAsSchedule(MatchInfo match, Instant now) {
        if (0 == jooq.update(MATCHES)
                .set(MATCHES.STATE, Game)
                .set(MATCHES.STARTED, Optional.of(now))
                .where(MATCHES.MID.eq(match.getMid()),
                        MATCHES.STATE.eq(Place))
                .execute()) {
            throw internalError("Match "
                    + match.getMid() + " is not in place state");
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
    public void deleteAllByTid(OpenTournamentMemState tournament, DbUpdater batch) {
        batch
                .exec(DbUpdate.builder()
                        .query(jooq.deleteFrom(SET_SCORE)
                                .where(SET_SCORE.MID.in(tournament.getMatches().keySet())))
                        .build())
                .exec(DbUpdate.builder()
                        .query(jooq.deleteFrom(MATCHES)
                        .where(MATCHES.TID.eq(tournament.getTid())))
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
