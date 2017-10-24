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
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.db.DbUpdateSql.JUST_A_ROW;
import static org.dan.ping.pong.sys.db.DbUpdateSql.NON_ZERO_ROWS;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import ord.dan.ping.pong.jooq.tables.Users;
import org.dan.ping.pong.app.table.TableLink;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.Uid;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.sys.db.DbUpdateSql;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class MatchDaoServer implements MatchDao {
    private static final Users ENEMY_USER = USERS.as("enemy_user");

    @Inject
    private DSLContext jooq;

    @Override
    public int createGroupMatch(int tid, int gid, int cid, int priorityGroup, Uid uid1, Uid uid2) {
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

    @Override
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

    @Override
    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<OpenMatchForJudge> findOpenMatchesFurJudge(Uid adminUid) {
        return jooq.select(MATCHES.MID, MATCHES.TID, MATCHES.STARTED,
                TABLES.TABLE_ID, TABLES.LABEL, TOURNAMENT.RULES,
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
                .leftJoin(TABLES)
                .on(MATCHES.MID.eq(TABLES.MID.cast(Integer.class)))
                .where(TOURNAMENT_ADMIN.UID.eq(adminUid),
                        MATCHES.STATE.eq(Game))
                .orderBy(MATCHES.STARTED)
                .fetch()
                .map(r -> OpenMatchForJudge.builder()
                        .mid(r.get(MATCHES.MID))
                        .tid(r.get(MATCHES.TID))
                        .matchScore(r.get(TOURNAMENT.RULES).getMatch().getMinGamesToWin())
                        .started(r.get(MATCHES.STARTED).get())
                        .type(r.get(MATCHES.TYPE))
                        .table(ofNullable(r.get(TABLES.TABLE_ID))
                                .map(tableId ->
                                        TableLink.builder()
                                                .id(tableId)
                                                .label(r.get(TABLES.LABEL))
                                        .build()))
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

    @Override
    public void changeStatus(int mid, MatchState state, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .mustAffectRows(NON_ZERO_ROWS)
                .logBefore(() -> log.info("Put match {} into {}", mid, state))
                .query(jooq.update(MATCHES)
                        .set(MATCHES.STATE, state)
                        .where(MATCHES.MID.eq(mid)))
                .build());
    }

    @Override
    public Optional<Uid> scoreSet(OpenTournamentMemState tournament, MatchInfo matchInfo,
            DbUpdater batch, FinalMatchScore matchScore) {
        final Optional<Uid> winUidO = matchInfo.addSetScore(
                matchScore.getScores(), tournament.getRule().getMatch());
        insertSetScore(batch, matchScore);
        return winUidO;
    }

    @Override
    public void completeMatch(int mid, Uid winUid, Instant now, DbUpdater batch, MatchState... expected) {
        batch.exec(DbUpdateSql.builder()
                .mustAffectRows(JUST_A_ROW)
                .logBefore(() -> log.info("Match {} won uid {} if {}", mid, winUid, expected))
                .query(jooq.update(MATCHES)
                        .set(MATCHES.STATE, MatchState.Over)
                        .set(MATCHES.UID_WIN, winUid)
                        .set(MATCHES.ENDED, Optional.of(now))
                        .where(MATCHES.MID.eq(mid),
                                MATCHES.STATE.in(expected)))
                .build());
    }

    private void insertSetScore(DbUpdater batch, FinalMatchScore matchScore) {
        matchScore.getScores().forEach(score -> batch.exec(
                DbUpdateSql.builder()
                        .query(jooq.insertInto(SET_SCORE, SET_SCORE.MID,
                                SET_SCORE.UID, SET_SCORE.GAMES)
                                .values(matchScore.getMid(), score.getUid(), score.getScore()))
                        .build()));
    }

    @Override
    public void markAsSchedule(MatchInfo match, DbUpdater batch) {
        batch.exec(
                DbUpdateSql.builder()
                        .logBefore(() -> log.info("Match {} between {} began",
                                match.getMid(), match.getParticipantIdScore().keySet()))
                        .onFailure(u -> internalError("Match "
                                + match.getMid() + " is not in place state"))
                        .query(jooq.update(MATCHES)
                                .set(MATCHES.STATE, match.getState())
                                .set(MATCHES.STARTED, match.getStartedAt())
                                .where(MATCHES.MID.eq(match.getMid()),
                                        MATCHES.STATE.eq(Place)))
                        .build());
    }

    @Override
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

    @Override
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

    @Override
    public void deleteAllByTid(OpenTournamentMemState tournament, DbUpdater batch, int size) {
        batch
                .exec(DbUpdateSql.builder()
                        .mustAffectRows(empty())
                        .query(jooq.deleteFrom(SET_SCORE)
                                .where(SET_SCORE.MID.in(tournament.getMatches().keySet())))
                        .build())
                .exec(DbUpdateSql.builder()
                        .mustAffectRows(Optional.of(size))
                        .query(jooq.deleteFrom(MATCHES)
                        .where(MATCHES.TID.eq(tournament.getTid())))
                        .build());
    }

    @Override
    public void setParticipant(int n, int mid, Uid uid, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .mustAffectRows(NON_ZERO_ROWS)
                .query(jooq.update(MATCHES)
                        .set(n == 0
                                ? MATCHES.UID_LESS
                                : MATCHES.UID_MORE, uid)
                        .where(MATCHES.MID.eq(mid)))
                .build());
    }

    @Override
    public List<MatchInfo> load(Tid tid) {
        return jooq.select(MATCHES.MID, MATCHES.GID, MATCHES.CID,
                MATCHES.WIN_MID, MATCHES.LOSE_MID, MATCHES.PRIORITY,
                MATCHES.STATE, MATCHES.TYPE, MATCHES.ENDED,
                MATCHES.UID_LESS, MATCHES.UID_MORE, MATCHES.UID_WIN,
                MATCHES.STARTED)
                .from(MATCHES)
                .where(MATCHES.TID.eq(tid.getTid()))
                .fetch()
                .map(r -> {
                    final Map<Uid, List<Integer>> uids = new HashMap<>();
                    ofNullable(r.get(MATCHES.UID_LESS))
                            .filter(uid -> uid.getId() > 0)
                            .ifPresent(uid -> uids.put(uid, new ArrayList<>()));
                    ofNullable(r.get(MATCHES.UID_MORE))
                            .filter(uid -> uid.getId() > 0)
                            .ifPresent(uid -> uids.put(uid, new ArrayList<>()));
                    return MatchInfo.builder()
                            .mid(r.get(MATCHES.MID))
                            .cid(r.get(MATCHES.CID))
                            .gid(r.get(MATCHES.GID))
                            .state(r.get(MATCHES.STATE))
                            .type(r.get(MATCHES.TYPE))
                            .loserMid(r.get(MATCHES.LOSE_MID))
                            .priority(r.get(MATCHES.PRIORITY))
                            .winnerMid(r.get(MATCHES.WIN_MID))
                            .winnerId(ofNullable(r.get(MATCHES.UID_WIN)))
                            .startedAt(r.get(MATCHES.STARTED))
                            .endedAt(r.get(MATCHES.ENDED))
                            .participantIdScore(uids)
                            .tid(tid.getTid())
                            .build();
                });
    }

    @Override
    public void deleteSets(DbUpdater batch, MatchInfo minfo, int setNumber) {
        minfo.getParticipantIdScore().keySet().forEach(uid -> {
            batch.exec(DbUpdateSql.builder()
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

    @Override
    public void removeSecondParticipant(DbUpdater batch, int mid, Uid uidKeep) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(MATCHES)
                        .set(MATCHES.UID_LESS, uidKeep)
                        .set(MATCHES.UID_MORE, (Uid) null)
                        .where(MATCHES.MID.eq(mid)))
                .build());
    }

    @Override
    public void removeParticipants(DbUpdater batch, int mid) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(MATCHES)
                        .set(MATCHES.UID_LESS, (Uid) null)
                        .set(MATCHES.UID_MORE, (Uid) null)
                        .where(MATCHES.MID.eq(mid)))
                .build());
    }

    @Override
    public void removeScores(DbUpdater batch, int mid, Uid uid) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.deleteFrom(SET_SCORE)
                        .where(SET_SCORE.MID.eq(mid), SET_SCORE.UID.eq(uid)))
                .build());
    }
}
