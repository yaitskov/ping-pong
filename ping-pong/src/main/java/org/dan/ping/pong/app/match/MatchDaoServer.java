package org.dan.ping.pong.app.match;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.match.MatchInfo.MID;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.match.MatchType.Grup;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;
import static org.dan.ping.pong.jooq.Tables.SET_SCORE;
import static org.dan.ping.pong.jooq.Tables.USERS;
import static org.dan.ping.pong.jooq.tables.Matches.MATCHES;
import static org.dan.ping.pong.sys.db.DbUpdateSql.JUST_A_ROW;
import static org.dan.ping.pong.sys.db.DbUpdateSql.NON_ZERO_ROWS;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.jooq.tables.Users;
import org.dan.ping.pong.jooq.tables.records.MatchesRecord;
import org.dan.ping.pong.sys.db.DbUpdateSql;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.collection.MaxValue;
import org.jooq.DSLContext;
import org.jooq.TableField;

import java.util.ArrayList;
import java.util.Collection;
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

    public void createMatch(MatchInfo matchInfo, DbUpdater batch) {
        final List<Bid> participantBids = matchInfo
                .participants().collect(toList());
        batch.exec(DbUpdateSql.builder()
                .query(jooq.insertInto(
                        MATCHES,
                        MATCHES.MID,
                        MATCHES.TID,
                        MATCHES.GID,
                        MATCHES.CID,
                        MATCHES.STATE,
                        MATCHES.PRIORITY,
                        MATCHES.LEVEL,
                        MATCHES.TYPE,
                        MATCHES.BID_LESS,
                        MATCHES.BID_MORE,
                        MATCHES.TAG,
                        MATCHES.WIN_MID,
                        MATCHES.LOSE_MID,
                        MATCHES.BID_WIN,
                        MATCHES.STARTED,
                        MATCHES.ENDED)
                        .values(matchInfo.getMid(),
                                matchInfo.getTid(),
                                matchInfo.getGid().map(Gid::intValue),
                                matchInfo.getCid(),
                                matchInfo.getState(),
                                matchInfo.getPriority(),
                                matchInfo.getLevel(),
                                matchInfo.getType(),
                                participantBids.size() > 0 ? participantBids.get(0) : null,
                                participantBids.size() > 1 ? participantBids.get(1) : null,
                                matchInfo.getTag().orElse(null),
                                matchInfo.getWinnerMid(),
                                matchInfo.getLoserMid(),
                                matchInfo.getWinnerId().orElse(null),
                                matchInfo.getStartedAt(),
                                matchInfo.getEndedAt()))
                .onFailure((u) -> internalError("Import match", MID, matchInfo.getMid()))
                .mustAffectRows(Optional.of(1))
                .logBefore(() -> log.info("Import match {} of tournament {}",
                        matchInfo.getMid(), matchInfo.getTid()))
                .build());
    }

    @Override
    public void createGroupMatch(DbUpdater batch, Mid mid, Tid tid, Gid gid, Cid cid, int priorityGroup,
            Bid bid1, Bid bid2, Optional<MatchTag> tag, MatchState place) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.insertInto(MATCHES, MATCHES.MID, MATCHES.TID,
                        MATCHES.GID, MATCHES.CID,
                        MATCHES.STATE, MATCHES.PRIORITY, MATCHES.TYPE,
                        MATCHES.BID_LESS, MATCHES.BID_MORE, MATCHES.TAG)
                        .values(mid, tid, Optional.of(gid).map(Gid::intValue), cid, place, priorityGroup,
                                Grup, bid1, bid2, tag.orElse(null)))
                .onFailure((u) -> internalError("Create group match", MID, mid))
                .mustAffectRows(Optional.of(1))
                .logBefore(() -> log.info("Create match {} in group {} of tournament {}",
                        mid, gid, tid))
                .build());
    }

    @Override
    public void createPlayOffMatch(DbUpdater batch, Mid mid, Tid tid, Cid cid,
            Optional<Mid> winMid, Optional<Mid> loseMid,
            int priority, int level, MatchType type,
            Optional<MatchTag> tag, MatchState draft) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.insertInto(MATCHES, MATCHES.MID, MATCHES.TID, MATCHES.CID,
                        MATCHES.PRIORITY, MATCHES.STATE, MATCHES.WIN_MID,
                        MATCHES.LOSE_MID, MATCHES.LEVEL, MATCHES.TYPE, MATCHES.TAG)
                        .values(mid, tid, cid, priority, draft, winMid, loseMid, level,
                                type, tag.orElse(null)))
                .onFailure((u) -> internalError("Create play off match", MID, mid))
                .mustAffectRows(Optional.of(1))
                .logBefore(() -> log.info("Create match {} in playoff of tournament {}",
                        mid, tid))
                .build());
    }

    @Override
    public void changeStatus(MatchInfo mInfo, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .mustAffectRows(NON_ZERO_ROWS)
                .logBefore(() -> log.info("Put match {} into {}",
                        mInfo.getMid(), mInfo.getState()))
                .query(jooq.update(MATCHES)
                        .set(MATCHES.STATE, mInfo.getState())
                        .where(MATCHES.TID.eq(mInfo.getTid()),
                                MATCHES.MID.eq(mInfo.getMid())))
                .build());
    }

    @Override
    public void scoreSet(TournamentMemState tournament, MatchInfo matchInfo,
            DbUpdater batch, List<IdentifiedScore> scores) {
        insertSetScore(batch, tournament.getTid(), matchInfo.getMid(), scores);
    }

    @Override
    public void insertScores(MatchInfo mInfo, DbUpdater batch) {
        mInfo.getParticipantIdScore().forEach(
                (bid, sets) ->
                        sets.forEach(games -> batch.exec(
                                DbUpdateSql.builder()
                                        .query(jooq.insertInto(SET_SCORE, SET_SCORE.TID,
                                                SET_SCORE.MID, SET_SCORE.BID,
                                                SET_SCORE.GAMES)
                                                .values(mInfo.getTid(), mInfo.getMid(),
                                                        bid, games))
                                        .build())));
    }

    @Override
    public void completeMatch(MatchInfo mInfo, DbUpdater batch, Set<MatchState> expected) {
        batch.exec(DbUpdateSql.builder()
                .mustAffectRows(JUST_A_ROW)
                .logBefore(() -> log.info("Match {} won uid {} if {}",
                        mInfo.getMid(), mInfo.winnerId(), expected))
                .query(jooq.update(MATCHES)
                        .set(MATCHES.STATE, MatchState.Over)
                        .set(MATCHES.BID_WIN, mInfo.winnerId())
                        .set(MATCHES.ENDED, mInfo.getEndedAt())
                        .where(MATCHES.TID.eq(mInfo.getTid()),
                                MATCHES.MID.eq(mInfo.getMid()),
                                MATCHES.STATE.in(expected)))
                .build());
    }

    private void insertSetScore(DbUpdater batch, Tid tid, Mid mid, List<IdentifiedScore> scores) {
        scores.forEach(score -> batch.exec(
                DbUpdateSql.builder()
                        .query(jooq.insertInto(SET_SCORE, SET_SCORE.TID, SET_SCORE.MID,
                                SET_SCORE.BID, SET_SCORE.GAMES)
                                .values(tid, mid, score.getBid(), score.getScore()))
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
                                .where(MATCHES.TID.eq(match.getTid()),
                                        MATCHES.MID.eq(match.getMid()),
                                        MATCHES.STATE.eq(Place)))
                        .build());
    }

    @Override
    public void deleteByIds(Tid tid, Collection<Mid> mids, DbUpdater batch) {
        if (mids.isEmpty()) {
            return;
        }
        log.info("Remove mids {}", mids);
        batch
                .exec(DbUpdateSql.builder()
                        .mustAffectRows(empty())
                        .query(jooq.deleteFrom(SET_SCORE)
                                .where(SET_SCORE.TID.eq(tid),
                                        SET_SCORE.MID.in(mids)))
                        .build())
                .exec(DbUpdateSql.builder()
                        .mustAffectRows(NON_ZERO_ROWS)
                        .query(jooq.deleteFrom(MATCHES)
                                .where(MATCHES.TID.eq(tid),
                                        MATCHES.MID.in(mids)))
                        .build());
    }

    @Override
    public void deleteAllByTid(TournamentMemState tournament, DbUpdater batch, int size) {
        batch
                .exec(DbUpdateSql.builder()
                        .mustAffectRows(empty())
                        .query(jooq.deleteFrom(SET_SCORE)
                                .where(SET_SCORE.TID.eq(tournament.getTid())))
                        .build())
                .exec(DbUpdateSql.builder()
                        .mustAffectRows(Optional.of(size))
                        .query(jooq.deleteFrom(MATCHES)
                        .where(MATCHES.TID.eq(tournament.getTid())))
                        .build());
    }

    @Override
    public void setParticipant(MatchInfo mInfo, Bid bid, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .mustAffectRows(NON_ZERO_ROWS)
                .query(jooq.update(MATCHES)
                        .set(pickField(mInfo.numberOfParticipants()), bid)
                        .where(MATCHES.TID.eq(mInfo.getTid()),
                                MATCHES.MID.eq(mInfo.getMid())))
                .build());
    }

    private TableField<MatchesRecord, Bid> pickField(int n) {
        switch (n) {
            case 1:
                return MATCHES.BID_LESS;
            case 2:
                return MATCHES.BID_MORE;
            default:
                throw internalError("n scouhd be 1 or 2");
        }
    }

    @Override
    public List<MatchInfo> load(Tid tid, MaxValue<Mid> maxMid) {
        return jooq.select(MATCHES.MID, MATCHES.GID, MATCHES.CID,
                MATCHES.WIN_MID, MATCHES.LOSE_MID, MATCHES.PRIORITY,
                MATCHES.STATE, MATCHES.TYPE, MATCHES.ENDED,
                MATCHES.BID_LESS, MATCHES.BID_MORE, MATCHES.BID_WIN,
                MATCHES.STARTED, MATCHES.LEVEL, MATCHES.TAG)
                .from(MATCHES)
                .where(MATCHES.TID.eq(tid))
                .fetch()
                .map(r -> {
                    final Map<Bid, List<Integer>> uids = new HashMap<>();
                    ofNullable(r.get(MATCHES.BID_LESS))
                            .filter(uid -> uid.intValue() > 0)
                            .ifPresent(uid -> uids.put(uid, new ArrayList<>()));
                    ofNullable(r.get(MATCHES.BID_MORE))
                            .filter(uid -> uid.intValue() > 0)
                            .ifPresent(uid -> uids.put(uid, new ArrayList<>()));
                    return MatchInfo.builder()
                            .mid(maxMid.apply(r.get(MATCHES.MID)))
                            .cid(r.get(MATCHES.CID))
                            .gid(r.get(MATCHES.GID).map(Gid::new))
                            .tag(Optional.ofNullable(r.get(MATCHES.TAG)))
                            .state(r.get(MATCHES.STATE))
                            .type(r.get(MATCHES.TYPE))
                            .loserMid(r.get(MATCHES.LOSE_MID))
                            .level(ofNullable(r.get(MATCHES.LEVEL)).orElse(0))
                            .priority(r.get(MATCHES.PRIORITY))
                            .winnerMid(r.get(MATCHES.WIN_MID))
                            .winnerId(ofNullable(r.get(MATCHES.BID_WIN)))
                            .startedAt(r.get(MATCHES.STARTED))
                            .endedAt(r.get(MATCHES.ENDED))
                            .losersMeet(uids.size() == 1 && uids.containsKey(FILLER_LOSER_BID))
                            .participantIdScore(uids)
                            .tid(tid)
                            .build();
                });
    }

    @Override
    public void deleteSets(DbUpdater batch, MatchInfo mInfo, int setNumber) {
        final int limit = mInfo.playedSets() - setNumber;
        mInfo.participants().forEach(bid -> {
            final List<Integer> participantScore = mInfo.getParticipantScore(bid);
            if (participantScore.isEmpty()) {
                log.info("Bid {} in mid {} has no scores", bid, mInfo.getMid());
                return;
            }
            batch.exec(DbUpdateSql.builder()
                    .mustAffectRows(empty())
                    .logBefore(() -> log.info("Delete sets after {} in mid {} for uid {}",
                            setNumber, mInfo.getMid(), bid))
                    .query(jooq.query("delete from "
                                    + SET_SCORE.getSchema().getName() + "." + SET_SCORE.getName()
                                    + " where " + SET_SCORE.TID.getName() + " = ? and "
                                    + SET_SCORE.MID.getName() + " = ? and "
                                    + SET_SCORE.BID.getName() + " = ? order by "
                                    + SET_SCORE.SET_ID.getName() + " desc limit ?",
                            mInfo.getTid(), mInfo.getMid(), bid, limit))
                    .build());
        });
    }

    @Override
    public void removeSecondParticipant(DbUpdater batch, MatchInfo mInfo, Bid bidKeep) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(MATCHES)
                        .set(MATCHES.BID_LESS, bidKeep)
                        .set(MATCHES.BID_MORE, (Bid) null)
                        .where(MATCHES.TID.eq(mInfo.getTid()),
                                MATCHES.MID.eq(mInfo.getMid())))
                .build());
    }

    @Override
    public void removeParticipants(DbUpdater batch, MatchInfo mInfo) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(MATCHES)
                        .set(MATCHES.BID_LESS, (Bid) null)
                        .set(MATCHES.BID_MORE, (Bid) null)
                        .where(MATCHES.TID.eq(mInfo.getTid()),
                                MATCHES.MID.eq(mInfo.getMid())))
                .build());
    }

    @Override
    public void removeScores(DbUpdater batch, MatchInfo mInfo, Bid bid, int played) {
        batch.exec(DbUpdateSql.builder()
                .logBefore(() -> log.info("Delete all scores for mid {} uid {}",
                        mInfo.getMid(), bid))
                .mustAffectRows(Optional.of(played))
                .query(jooq.deleteFrom(SET_SCORE)
                        .where(SET_SCORE.TID.eq(mInfo.getTid()),
                                SET_SCORE.MID.eq(mInfo.getMid()),
                                SET_SCORE.BID.eq(bid)))
                .build());
    }

    @Override
    public void setWinnerId(MatchInfo mInfo, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(MATCHES)
                        .set(MATCHES.BID_WIN, mInfo.getWinnerId().orElse(null))
                        .where(MATCHES.TID.eq(mInfo.getTid()),
                                MATCHES.MID.eq(mInfo.getMid())))
                .build());
    }
}
