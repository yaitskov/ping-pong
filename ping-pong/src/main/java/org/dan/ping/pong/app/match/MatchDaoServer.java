package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.BID;
import static ord.dan.ping.pong.jooq.Tables.SET_SCORE;
import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT;
import static ord.dan.ping.pong.jooq.Tables.USERS;
import static ord.dan.ping.pong.jooq.tables.Matches.MATCHES;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.app.match.MatchState.Draft;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.match.MatchType.Grup;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.db.DbUpdateSql.JUST_A_ROW;
import static org.dan.ping.pong.sys.db.DbUpdateSql.NON_ZERO_ROWS;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import ord.dan.ping.pong.jooq.tables.Users;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.bid.Uid;
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
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class MatchDaoServer implements MatchDao {
    private static final Users ENEMY_USER = USERS.as("enemy_user");

    @Inject
    private DSLContext jooq;

    @Override
    public Mid createGroupMatch(Tid tid, int gid, int cid, int priorityGroup, Uid uid1, Uid uid2) {
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
    public Mid createPlayOffMatch(Tid tid, Integer cid,
            Optional<Mid> winMid, Optional<Mid> loseMid,
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
    public void changeStatus(Mid mid, MatchState state, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .mustAffectRows(NON_ZERO_ROWS)
                .logBefore(() -> log.info("Put match {} into {}", mid, state))
                .query(jooq.update(MATCHES)
                        .set(MATCHES.STATE, state)
                        .where(MATCHES.MID.eq(mid)))
                .build());
    }

    @Override
    public Optional<Uid> scoreSet(TournamentMemState tournament, MatchInfo matchInfo,
            DbUpdater batch, List<IdentifiedScore> scores) {
        final Optional<Uid> winUidO = matchInfo.addSetScore(
                scores, tournament.getRule().getMatch());
        insertSetScore(batch, matchInfo.getMid(), scores);
        return winUidO;
    }

    @Override
    public void insertScores(MatchInfo mInfo, DbUpdater batch) {
        mInfo.getParticipantIdScore().forEach(
                (uid, sets) ->
                        sets.forEach(games -> batch.exec(
                                DbUpdateSql.builder()
                                        .query(jooq.insertInto(SET_SCORE, SET_SCORE.MID,
                                                SET_SCORE.UID, SET_SCORE.GAMES)
                                                .values(mInfo.getMid(), uid, games))
                                        .build())));
    }

    @Override
    public void completeMatch(Mid mid, Uid winUid, Instant now, DbUpdater batch, Set<MatchState> expected) {
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

    private void insertSetScore(DbUpdater batch, Mid mid, List<IdentifiedScore> scores) {
        scores.forEach(score -> batch.exec(
                DbUpdateSql.builder()
                        .query(jooq.insertInto(SET_SCORE, SET_SCORE.MID,
                                SET_SCORE.UID, SET_SCORE.GAMES)
                                .values(mid, score.getUid(), score.getScore()))
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
    public List<CompleteMatch> findCompleteMatches(Tid tid) {
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
    public List<UserLink> findWinners(Tid tid) {
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
    public void deleteAllByTid(TournamentMemState tournament, DbUpdater batch, int size) {
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
    public void setParticipant(int n, Mid mid, Uid uid, DbUpdater batch) {
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
                MATCHES.STARTED, MATCHES.LEVEL)
                .from(MATCHES)
                .where(MATCHES.TID.eq(tid))
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
                            .level(r.get(MATCHES.LEVEL))
                            .priority(r.get(MATCHES.PRIORITY))
                            .winnerMid(r.get(MATCHES.WIN_MID))
                            .winnerId(ofNullable(r.get(MATCHES.UID_WIN)))
                            .startedAt(r.get(MATCHES.STARTED))
                            .endedAt(r.get(MATCHES.ENDED))
                            .participantIdScore(uids)
                            .tid(tid)
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
                            minfo.getMid(), uid, minfo.getPlayedSets() - setNumber))
                    .build());
        });
    }

    @Override
    public void removeSecondParticipant(DbUpdater batch, Mid mid, Uid uidKeep) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(MATCHES)
                        .set(MATCHES.UID_LESS, uidKeep)
                        .set(MATCHES.UID_MORE, (Uid) null)
                        .where(MATCHES.MID.eq(mid)))
                .build());
    }

    @Override
    public void removeParticipants(DbUpdater batch, Mid mid) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(MATCHES)
                        .set(MATCHES.UID_LESS, (Uid) null)
                        .set(MATCHES.UID_MORE, (Uid) null)
                        .where(MATCHES.MID.eq(mid)))
                .build());
    }

    @Override
    public void removeScores(DbUpdater batch, Mid mid, Uid uid) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.deleteFrom(SET_SCORE)
                        .where(SET_SCORE.MID.eq(mid), SET_SCORE.UID.eq(uid)))
                .build());
    }

    @Override
    public void setWinnerId(MatchInfo mInfo, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(MATCHES)
                        .set(MATCHES.UID_WIN, mInfo.getWinnerId().orElse(null))
                        .where(MATCHES.TID.eq(mInfo.getTid()),
                                MATCHES.MID.eq(mInfo.getMid())))
                .build());
    }
}
