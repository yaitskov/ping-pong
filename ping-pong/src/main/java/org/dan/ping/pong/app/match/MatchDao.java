package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.BID;
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
import ord.dan.ping.pong.jooq.tables.Users;
import org.dan.ping.pong.app.table.TableLink;
import org.dan.ping.pong.app.tournament.DbUpdate;
import org.dan.ping.pong.app.tournament.DbUpdater;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.user.UserLink;
import org.jooq.DSLContext;
import org.jooq.Record11;
import org.jooq.impl.DSL;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class MatchDao {
    private static final Users ENEMY_USER = USERS.as("enemy_user");
    public static final int FIRST_PLAY_OFF_MATCH_LEVEL = 1;

    @Inject
    private DSLContext jooq;

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
                TABLES.LABEL, MATCHES.TYPE, USERS.UID, USERS.NAME,
                ENEMY_USER.UID, ENEMY_USER.NAME,
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
                        .enemy(enemy(uid, r))
                        .build());
    }

    private Optional<UserLink> enemy(int uid,
            Record11<Integer, Integer, Integer, String, MatchType,
                    Integer, String, Integer, String, MatchState, Integer> r) {
        final Optional<Integer> enemId = ofNullable(r.get(ENEMY_USER.UID));
        final Optional<Integer> userId = ofNullable(r.get(USERS.UID));

        if (Optional.of(uid).equals(enemId)) {
            return userId.map(id -> UserLink.builder()
                    .name(r.get(USERS.NAME))
                    .uid(id)
                    .build());
        } else if (Optional.of(uid).equals(userId)) {
            return enemId.map(id -> UserLink.builder()
                    .name(r.get(ENEMY_USER.NAME))
                    .uid(id)
                    .build());
        }
        return empty();
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

    public void completeMatch(int mid, int winUid, Instant now, DbUpdater batch, MatchState... expected) {
        batch.exec(DbUpdate.builder()
                .mustAffectRows(JUST_A_ROW)
                .logBefore(() -> log.info("Match {} won uid {} if {}", mid, winUid, expected))
                .query(jooq.update(MATCHES)
                        .set(MATCHES.STATE, MatchState.Over)
                        .set(MATCHES.UID_WIN, Optional.of(winUid))
                        .set(MATCHES.ENDED, Optional.of(now))
                        .where(MATCHES.MID.eq(mid),
                                MATCHES.STATE.in(expected)))
                .build());
    }

    private void insertSetScore(DbUpdater batch, FinalMatchScore matchScore) {
        matchScore.getScores().forEach(score -> batch.exec(
                jooq.insertInto(SET_SCORE, SET_SCORE.MID,
                        SET_SCORE.UID, SET_SCORE.GAMES)
                        .values(matchScore.getMid(), score.getUid(), score.getScore())));
    }

    public void markAsSchedule(MatchInfo match, DbUpdater batch) {
        batch.exec(
                DbUpdate.builder()
                        .logBefore(() -> log.info("Match {} began", match.getMid()))
                        .onFailure(u -> internalError("Match "
                                + match.getMid() + " is not in place state"))
                        .query(jooq.update(MATCHES)
                                .set(MATCHES.STATE, match.getState())
                                .set(MATCHES.STARTED, match.getStartedAt())
                                .where(MATCHES.MID.eq(match.getMid()),
                                        MATCHES.STATE.eq(Place)))
                        .build());
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

    public void deleteAllByTid(OpenTournamentMemState tournament, DbUpdater batch, int size) {
        batch
                .exec(DbUpdate.builder()
                        .mustAffectRows(empty())
                        .query(jooq.deleteFrom(SET_SCORE)
                                .where(SET_SCORE.MID.in(tournament.getMatches().keySet())))
                        .build())
                .exec(DbUpdate.builder()
                        .mustAffectRows(Optional.of(size))
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
                .map(r -> {
                    final Map<Integer, List<Integer>> uids = new HashMap<>();
                    if (r.get(MATCHES.UID_LESS) > 0) {
                        uids.put(r.get(MATCHES.UID_LESS), new ArrayList<>());
                    }
                    if (r.get(MATCHES.UID_MORE) > 0) {
                        uids.put(r.get(MATCHES.UID_MORE), new ArrayList<>());
                    }
                    return MatchInfo.builder()
                            .mid(r.get(MATCHES.MID))
                            .cid(r.get(MATCHES.CID))
                            .gid(r.get(MATCHES.GID))
                            .state(r.get(MATCHES.STATE))
                            .type(r.get(MATCHES.TYPE))
                            .loserMid(r.get(MATCHES.LOSE_MID))
                            .winnerMid(r.get(MATCHES.WIN_MID))
                            .winnerId(r.get(MATCHES.UID_WIN))
                            .participantIdScore(uids)
                            .tid(tid.getTid())
                            .build();
                });
    }

    public void deleteSets(DbUpdater batch, MatchInfo minfo, int setNumber) {
        minfo.getParticipantIdScore().keySet().forEach(uid -> {
            batch.exec(DbUpdate.builder()
                    .mustAffectRows(NON_ZERO_ROWS)
                    .logBefore(() -> log.info("Delete sets after {} in mid {}",
                            setNumber, minfo.getMid()))
                    .query(jooq.query("delete from "
                                    + SET_SCORE.getSchema().getName() + "." + SET_SCORE.getName()
                                    + " where " + SET_SCORE.MID.getName() + " = ? and "
                                    + SET_SCORE.UID.getName() + " = ? order by "
                                    + SET_SCORE.SET_ID.getName() + " desc limit ?",
                            minfo.getMid(), uid, minfo.getNumberOfSets() - setNumber))
                    .build());
        });
    }

    public void removeSecondParticipant(DbUpdater batch, int mid, int uidKeep) {
        batch.exec(DbUpdate.builder()
                .query(jooq.update(MATCHES)
                        .set(MATCHES.UID_LESS, uidKeep)
                        .set(MATCHES.UID_MORE, (Integer) null)
                        .where(MATCHES.MID.eq(mid)))
                .build());
    }

    public void removeParticipants(DbUpdater batch, int mid) {
        batch.exec(DbUpdate.builder()
                .query(jooq.update(MATCHES)
                        .set(MATCHES.UID_LESS, (Integer) null)
                        .set(MATCHES.UID_MORE, (Integer) null)
                        .where(MATCHES.MID.eq(mid)))
                .build());
    }

    public void removeScores(DbUpdater batch, int mid, int uid) {
        batch.exec(DbUpdate.builder()
                .query(jooq.deleteFrom(SET_SCORE)
                        .where(SET_SCORE.MID.eq(mid), SET_SCORE.UID.eq(uid)))
                .build());
    }
}
