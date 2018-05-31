package org.dan.ping.pong.app.bid;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.jooq.Tables.BID;
import static org.dan.ping.pong.jooq.Tables.CATEGORY;
import static org.dan.ping.pong.jooq.Tables.USERS;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.db.DbUpdateSql.JUST_A_ROW;
import static org.dan.ping.pong.sys.db.DbUpdateSql.NON_ZERO_ROWS;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.jooq.tables.records.BidRecord;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.sys.db.DbUpdateSql;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.jooq.DSLContext;
import org.jooq.UpdateConditionStep;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Slf4j
public class BidDaoServer implements BidDao {
    @Inject
    private DSLContext jooq;

    @Override
    public void setBidState(Tid tid, Uid uid, BidState target,
            Collection<BidState> expected, Instant now, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .logBefore(() -> log.info("Set bid status {} for {} if {}",
                        target, uid, expected))
                .onFailure(u -> badRequest("Participant status was not " + expected))
                .mustAffectRows(JUST_A_ROW)
                .query(jooq.update(BID)
                        .set(BID.STATE, target)
                        .set(BID.UPDATED, Optional.of(now))
                        .where(BID.UID.eq(uid),
                                BID.TID.eq(tid),
                                BID.STATE.in(expected)))
                .build());
    }

    @Override
    public void markParticipantsBusy(TournamentMemState tournament,
            Collection<Uid> uids, Instant now, DbUpdater batch) {
        final List<Uid> finalUids = uids.stream()
                .map(tournament::getBid)
                .filter(bid -> bid.getState() == Wait)
                .peek(bid -> bid.setBidState(Play))
                .map(ParticipantMemState::getUid)
                .collect(toList());
        if (finalUids.isEmpty()) {
            return;
        }
        batch.exec(DbUpdateSql.builder()
                .mustAffectRows(Optional.of(finalUids.size()))
                .onFailure(u -> internalError("One of uids " + finalUids + " was busy"))
                .logBefore(() -> log.info("Make uids {} as playing if it's waiting", finalUids))
                .query(jooq.update(BID)
                        .set(BID.STATE, Play)
                        .set(BID.UPDATED, Optional.of(now))
                        .where(BID.TID.eq(tournament.getTid()),
                                BID.UID.in(finalUids),
                                BID.STATE.eq(Wait)))
                .build());
    }

    @Override
    public void setGroupForUids(DbUpdater batch, int gid, Tid tid,
            List<ParticipantMemState> groupBids) {
        batch.exec(DbUpdateSql.builder()
                .query(setGid(gid, tid, groupBids))
                .logBefore(() -> log.info("update gid {} for {}", gid,
                        groupBids.stream()
                                .map(ParticipantMemState::getUid)
                                .collect(toList())))
                .mustAffectRows(of(1))
                .build());
    }

    @Override
    public void setGroupForUids(int gid, Tid tid, List<ParticipantMemState> groupBids) {
        setGid(gid, tid, groupBids).execute();
    }

    private UpdateConditionStep<BidRecord> setGid(int gid, Tid tid, List<ParticipantMemState> groupBids) {
        return jooq.update(BID)
                .set(BID.GID, Optional.of(gid))
                .where(BID.TID.eq(tid),
                        BID.UID.in(groupBids.stream()
                                .map(ParticipantMemState::getUid)
                                .map(Uid::getId)
                                .collect(Collectors.toList())));
    }

    @Override
    public void enlist(ParticipantMemState bid, Optional<Integer> providedRank, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .logBefore(() -> log.info("User {} enlisted to tournament {}", bid.getUid(), bid.getTid()))
                .mustAffectRows(NON_ZERO_ROWS)
                .query(jooq.insertInto(BID, BID.CID, BID.TID, BID.UID,
                        BID.STATE, BID.PROVIDED_RANK, BID.CREATED, BID.UPDATED, BID.GID)
                        .values(bid.getCid(), bid.getTid(), bid.getUid(),
                                bid.getBidState(), providedRank, bid.getEnlistedAt(),
                                Optional.of(bid.getUpdatedAt()), bid.getGid())
                        .onDuplicateKeyUpdate()
                        .set(BID.STATE, bid.getBidState())
                        .set(BID.UPDATED, Optional.of(bid.getUpdatedAt()))
                        .set(BID.GID, bid.getGid())
                        .set(BID.PROVIDED_RANK, providedRank)
                        .set(BID.CID, bid.getCid()))
                .build());
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<ParticipantState> findEnlisted(Tid tid) {
        return jooq.select(BID.UID, USERS.NAME, BID.STATE, CATEGORY.NAME, CATEGORY.CID)
                .from(BID)
                .innerJoin(USERS)
                .on(BID.UID.eq(USERS.UID))
                .innerJoin(CATEGORY)
                .on(BID.CID.eq(CATEGORY.CID))
                .where(BID.TID.eq(tid))
                .fetch()
                .map(r -> ParticipantState.builder()
                        .category(CategoryLink.builder()
                                .cid(r.get(CATEGORY.CID))
                                .name(r.get(CATEGORY.NAME))
                                .build())
                        .user(UserLink.builder()
                                .name(r.get(USERS.NAME))
                                .uid(r.get(BID.UID))
                                .build())
                        .state(r.get(BID.STATE))
                        .build());
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<DatedParticipantState> getParticipantInfo(Tid tid, Uid uid) {
        return ofNullable(jooq.select(BID.UID, USERS.NAME, BID.STATE,
                BID.CREATED, CATEGORY.NAME, CATEGORY.CID)
                .from(BID)
                .innerJoin(USERS)
                .on(BID.UID.eq(USERS.UID))
                .innerJoin(CATEGORY)
                .on(BID.CID.eq(CATEGORY.CID))
                .where(BID.TID.eq(tid), BID.UID.eq(uid))
                .fetchOne())
                .map(r -> DatedParticipantState.builder()
                        .category(CategoryLink.builder()
                                .cid(r.get(CATEGORY.CID))
                                .name(r.get(CATEGORY.NAME))
                                .build())
                        .enlistedAt(r.get(BID.CREATED))
                        .user(UserLink.builder()
                                .name(r.get(USERS.NAME))
                                .uid(r.get(BID.UID))
                                .build())
                        .state(r.get(BID.STATE))
                        .build());
    }

    @Override
    public void setCategory(SetCategory setCategory, Instant now, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(BID)
                        .set(BID.CID, setCategory.getCid())
                        .set(BID.UPDATED, Optional.of(now))
                        .where(BID.TID.eq(setCategory.getTid()),
                                BID.UID.eq(setCategory.getUid())))
                .build());
    }

    @Override
    public void resetStateByTid(Tid tid, Instant now, DbUpdater batch) {
        batch.exec(
                DbUpdateSql.builder()
                        .mustAffectRows(empty())
                        .query(jooq.update(BID)
                                .set(BID.STATE, BidState.Want)
                                .set(BID.GID, Optional.empty())
                                .set(BID.UPDATED, Optional.of(now))
                                .where(BID.TID.eq(tid), BID.STATE.ne(Quit))).build());
    }

    @Override
    public Map<Uid, ParticipantMemState> loadParticipants(Tid tid) {
        return jooq.select(BID.UID, BID.STATE, USERS.NAME, BID.GID, BID.CID,
                BID.CREATED, BID.UPDATED)
                .from(BID)
                .innerJoin(USERS).on(BID.UID.eq(USERS.UID))
                .where(BID.TID.eq(tid))
                .fetch()
                .stream()
                .collect(toMap(r -> r.get(BID.UID),
                        r -> ParticipantMemState.builder()
                                .tid(tid)
                                .cid(r.get(BID.CID))
                                .gid(r.get(BID.GID))
                                .enlistedAt(r.get(BID.CREATED))
                                .updatedAt(r.get(BID.UPDATED).orElse(r.get(BID.CREATED)))
                                .name(r.get(USERS.NAME))
                                .uid(r.get(BID.UID))
                                .bidState(r.get(BID.STATE))
                                .build()));
    }

    @Override
    public void renameParticipant(Uid uid, String newName, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(USERS)
                        .set(USERS.NAME, newName)
                        .where(USERS.UID.eq(uid)))
                .build());
    }
}
