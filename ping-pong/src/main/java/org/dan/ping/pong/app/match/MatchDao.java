package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.BID;
import static ord.dan.ping.pong.jooq.Tables.MATCH_SCORE;
import static ord.dan.ping.pong.jooq.Tables.TABLES;
import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT;
import static ord.dan.ping.pong.jooq.Tables.TOURNAMENT_ADMIN;
import static ord.dan.ping.pong.jooq.Tables.USERS;
import static ord.dan.ping.pong.jooq.tables.Matches.MATCHES;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.app.match.MatchState.Draft;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.match.MatchType.Group;
import static org.dan.ping.pong.app.match.MatchType.PlayOff;

import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import ord.dan.ping.pong.jooq.tables.Bid;
import ord.dan.ping.pong.jooq.tables.MatchScore;
import ord.dan.ping.pong.jooq.tables.Users;
import org.dan.ping.pong.app.score.MatchScoreDao;
import org.dan.ping.pong.app.table.TableLink;
import org.dan.ping.pong.app.user.UserLink;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

@Slf4j
public class MatchDao {
    private static final MatchScore ENEMY_SCORE = MATCH_SCORE.as("enemy");
    private static final Users ENEMY_USER = USERS.as("enemy_user");
    private static final int FIRST_PLAY_OFF_MATCH_LEVEL = 0;
    private static final MatchScore MS_2 = MATCH_SCORE.as("ms2");
    private static final Bid BID_2 = BID.as("bid2");
    private static final Field<Integer> UID_2 = BID_2.UID.as("uid2");
    private static final String WINNER_ID = "winnerId";
    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public int createGroupMatch(int tid, int gid, int cid, int priorityGroup) {
        log.info("Create a match in group {} of tournament {}", gid, tid);
        return jooq.insertInto(MATCHES, MATCHES.TID,
                MATCHES.GID, MATCHES.CID,
                MATCHES.STATE, MATCHES.PRIORITY)
                .values(tid, Optional.of(gid), cid, Place, priorityGroup)
                .returning()
                .fetchOne()
                .getMid();
    }

    @Transactional(TRANSACTION_MANAGER)
    public int createPlayOffMatch(int tid, Integer cid,
            Optional<Integer> winMid, Optional<Integer> loseMid,
            int priority, int level) {
        return jooq.insertInto(MATCHES, MATCHES.TID, MATCHES.CID,
                MATCHES.PRIORITY, MATCHES.STATE,
                MATCHES.WIN_MID, MATCHES.LOSE_MID, MATCHES.LEVEL)
                .values(tid, cid, priority, Draft, winMid, loseMid, level)
                .returning()
                .fetchOne()
                .getMid();
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<OpenMatchForJudge> findOpenMatchesFurJudge(int adminUid) {
        return jooq.select(MATCHES.MID, MATCHES.STARTED,
                TABLES.TABLE_ID, TABLES.LABEL,
                MATCHES.GID, USERS.UID, USERS.NAME,
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
                        .started(r.get(MATCHES.STARTED).get())
                        .type(r.get(MATCHES.GID).map(gid -> Group).orElse(PlayOff))
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
        return jooq.select(MATCHES.MID, TABLES.TABLE_ID, TABLES.LABEL,
                MATCHES.GID, ENEMY_USER.UID, ENEMY_USER.NAME,
                MATCHES.STATE)
                .from(MATCH_SCORE)
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
                .fetch()
                .map(r -> MyPendingMatch.builder()
                        .mid(r.get(MATCHES.MID))
                        .state(r.get(MATCHES.STATE))
                        .matchType(r.get(MATCHES.GID) == null ? PlayOff : Group)
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

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<PendingMatchInfo> selectForScheduling(int size, int tid) {
        return jooq
                .select(MATCHES.MID, MATCHES.CID, MATCHES.GID,
                        BID.UID, UID_2)
                .from(MATCHES)
                .innerJoin(MATCH_SCORE).on(MATCHES.MID.eq(MATCH_SCORE.MID))
                .innerJoin(MS_2).on(MATCHES.MID.eq(MS_2.MID))
                .innerJoin(BID).on(MATCH_SCORE.UID.eq(BID.UID),
                        MATCH_SCORE.CID.eq(BID.CID))
                .innerJoin(BID_2).on(MS_2.UID.eq(BID_2.UID),
                        MS_2.CID.eq(BID_2.CID))
                .where(MATCHES.TID.eq(tid), MATCHES.STATE.eq(Place),
                        MS_2.UID.ne(MATCH_SCORE.UID),
                        BID.STATE.eq(Wait), BID_2.STATE.eq(Wait))
                .orderBy(MATCHES.PRIORITY, MATCHES.MID)
                .limit(size)
                .fetch()
                .map(r -> PendingMatchInfo.builder()
                        .mid(r.get(MATCHES.MID))
                        .gid(r.get(MATCHES.GID))
                        .cid(r.get(MATCHES.CID))
                        .uids(asList(r.get(BID.UID), r.get(UID_2)))
                        .build() );
    }

    @Transactional(TRANSACTION_MANAGER)
    public void changeStatus(int mid, MatchState state) {
        log.info("Put match {} into {}", mid, state);
        jooq.update(MATCHES)
                .set(MATCHES.STATE, state)
                .where(MATCHES.MID.eq(mid))
                .execute();
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<MatchInfo> getById(int mid) {
        final List<Integer> participantIds = new ArrayList<>(2);
        final MatchInfo[] result = new MatchInfo[1];
        jooq.select(MATCHES.TID, MATCHES.STATE, MATCHES.GID,
                MATCHES.CID, MATCHES.LOSE_MID, MATCHES.WIN_MID,
                MATCH_SCORE.UID)
                .from(MATCHES)
                .leftJoin(MATCH_SCORE)
                .on(MATCHES.MID.eq(MATCH_SCORE.MID))
                .where(MATCHES.MID.eq(mid))
                .fetch().forEach(r -> {
            participantIds.add(r.get(MATCH_SCORE.UID));
            if (result[0] == null) {
                result[0] = MatchInfo.builder()
                        .gid(r.get(MATCHES.GID))
                        .mid(mid)
                        .cid(r.get(MATCHES.CID))
                        .state(r.get(MATCHES.STATE))
                        .tid(r.get(MATCHES.TID))
                        .winnerMid(r.get(MATCHES.WIN_MID))
                        .loserMid(r.get(MATCHES.LOSE_MID))
                        .participantIds(participantIds)
                        .build();
            }
        });
        return ofNullable(result[0]);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void complete(Instant now, FinalMatchScore matchScore) {
        final List<Integer> scores = matchScore.getScores()
                .stream().map(IdentifiedScore::getScore)
                .collect(Collectors.toList());
        final Iterator<Integer> diffs = asList(
                scores.get(0) - scores.get(1),
                scores.get(1) - scores.get(0))
                .iterator();
        final List<Integer> updates = Ints.asList(jooq.batch(
                Stream.concat(
                        Stream.of(jooq.update(MATCHES)
                                .set(MATCHES.STATE, MatchState.Over)
                                .set(MATCHES.ENDED, Optional.of(now))
                                .where(MATCHES.MID.eq(matchScore.getMid()),
                                        MATCHES.STATE.eq(Game))),
                        matchScore.getScores()
                                .stream().map(score ->
                                jooq.update(MATCH_SCORE)
                                        .set(MATCH_SCORE.UPDATED, Optional.of(now))
                                        .set(MATCH_SCORE.WON, diffs.next())
                                        .set(MATCH_SCORE.SETS_WON, score.getScore())
                                        .where(MATCH_SCORE.UID.eq(score.getUid()),
                                                MATCH_SCORE.MID.eq(matchScore.getMid()))))
                        .collect(Collectors.toList()))
                .execute());
        if (!updates.equals(asList(1,1,1))) {
            throw internalError("Match " + matchScore.getMid()
                    + " is not in the game state: " + updates);
        }
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<GroupMatchInfo> findMatchesInGroup(int gid) {
        return jooq.select(
                MATCHES.MID,
                MATCHES.STATE,
                DSL.select(MATCH_SCORE.UID)
                        .from(MATCH_SCORE)
                        .where(MATCH_SCORE.MID.eq(MATCHES.MID),
                                MATCH_SCORE.WON.gt(0))
                        .asField(WINNER_ID))
                .from(MATCHES)
                .where(MATCHES.GID.eq(Optional.of(gid)))
                .fetch()
                .map(r -> GroupMatchInfo.builder()
                        .mid(r.get(MATCHES.MID))
                        .state(r.get(MATCHES.STATE))
                        .winnerId(ofNullable(r.get(WINNER_ID, Integer.class)))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<PlayOffMatchInfo> findPlayOffMatches(int tid, int cid) {
        return jooq
                .select(MATCHES.MID, MATCH_SCORE.UID.count())
                .from(MATCHES).leftJoin(MATCH_SCORE)
                .on(MATCHES.MID.eq(MATCH_SCORE.MID))
                .where(MATCHES.TID.eq(tid),
                        MATCHES.CID.eq(cid),
                        MATCHES.GID.isNull(),
                        MATCHES.LEVEL.eq(FIRST_PLAY_OFF_MATCH_LEVEL))
                .groupBy(MATCHES.MID)
                .orderBy(MATCHES.MID)
                .fetch()
                .map(r -> PlayOffMatchInfo.builder()
                        .mid(r.get(MATCHES.MID))
                        .cid(cid)
                        .tid(tid)
                        .drafted(r.get(1, MATCH_SCORE.UID.getType())).build());
    }

    @Inject
    private MatchScoreDao matchScoreDao;

    @Transactional(TRANSACTION_MANAGER)
    public void goPlayOff(int winnerId, PlayOffMatchInfo playOffMatch) {
        matchScoreDao.createScore(playOffMatch.getMid(), winnerId,
                playOffMatch.getCid(), playOffMatch.getTid());
        if (playOffMatch.getDrafted() == 1) {
            changeStatus(playOffMatch.getMid(), MatchState.Place);
        }
        playOffMatch.incrementDrafted();
    }

    @Transactional(TRANSACTION_MANAGER)
    public void assignMatchForWinner(int winnerUid, MatchInfo matchInfo) {
        final int mid = matchInfo.getWinnerMid().get();
        final Optional<Integer> enemyUid = matchScoreDao.findEnemy(mid, winnerUid);
        matchScoreDao.createScore(mid,
                winnerUid, matchInfo.getCid(), matchInfo.getTid());
        if (enemyUid.isPresent()) {
            log.info("Uid {} will play against uid {} in mid {}",
                    winnerUid, enemyUid.get(), mid);
            changeStatus(mid, Place);
        }
    }

    @Transactional(TRANSACTION_MANAGER)
    public void markAsSchedule(int mid, Instant now) {
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
        return jooq.select(MATCHES.MID, MATCHES.STARTED,
                MATCHES.STARTED, MATCHES.ENDED,
                MATCHES.GID, ENEMY_USER.UID, ENEMY_USER.NAME)
                .from(TOURNAMENT)
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
                .where(TOURNAMENT.TID.eq(tid),
                        ENEMY_SCORE.UID.lt(MATCH_SCORE.UID),
                        MATCHES.STATE.eq(Over))
                .orderBy(MATCHES.STARTED)
                .fetch()
                .map(r -> CompleteMatch.builder()
                        .mid(r.get(MATCHES.MID))
                        .started(r.get(MATCHES.STARTED).get())
                        .ended(r.get(MATCHES.ENDED).get())
                        .type(r.get(MATCHES.GID) == null ? PlayOff : Group)
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
}
