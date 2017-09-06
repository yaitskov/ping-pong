package org.dan.ping.pong.app.bid;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static ord.dan.ping.pong.jooq.Tables.BID;
import static ord.dan.ping.pong.jooq.Tables.CATEGORY;
import static ord.dan.ping.pong.jooq.Tables.USERS;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.tournament.DbUpdate.JUST_2_ROWS;
import static org.dan.ping.pong.app.tournament.DbUpdate.JUST_A_ROW;
import static org.dan.ping.pong.app.tournament.DbUpdate.NON_ZERO_ROWS;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.category.CategoryInfo;
import org.dan.ping.pong.app.tournament.DbUpdate;
import org.dan.ping.pong.app.tournament.DbUpdater;
import org.dan.ping.pong.app.tournament.EnlistTournament;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.Uid;
import org.dan.ping.pong.app.user.UserLink;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Slf4j
public class BidDao {
    @Inject
    private DSLContext jooq;

    public void setBidState(int tid, int uid, BidState state,
            BidState expected, Instant now, DbUpdater batch) {
        setBidState(tid, uid, state, singletonList(expected), now, batch);
    }

    public void setBidState(int tid, int uid, BidState target,
            List<BidState> expected, Instant now, DbUpdater batch) {
        batch.exec(DbUpdate.builder()
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

    @Transactional(TRANSACTION_MANAGER)
    public void casByTid(BidState expected, BidState set, int tid, Instant now) {
        log.info("Put all {} bids of tid {} into {} state", expected, tid, set);
        if (0 == jooq.update(BID)
                .set(BID.STATE, set)
                .set(BID.UPDATED, Optional.of(now))
                .where(BID.TID.eq(tid), BID.STATE.eq(expected))
                .execute()) {
            throw badRequest("Tournament " + tid + " has no participants");
        }
    }

    @Transactional(TRANSACTION_MANAGER)
    public void markParticipantsBusy(OpenTournamentMemState tournament,
            Collection<Integer> uids, Instant now, DbUpdater batch) {
        uids.stream().map(tournament::getBid)
                .forEach(bid -> bid.setBidState(Play));
        batch.exec(DbUpdate.builder()
                .mustAffectRows(JUST_2_ROWS)
                .onFailure(u -> internalError("One of uids " + uids + " was busy"))
                .query(jooq.update(BID)
                        .set(BID.STATE, Play)
                        .set(BID.UPDATED, Optional.of(now))
                        .where(BID.TID.eq(tournament.getTid()),
                                BID.UID.in(uids),
                                BID.STATE.eq(Wait)))
                .build());
    }

    public int setStatesAfterGroup(Integer gid, int tid, List<Integer> winnerIds, Instant now, DbUpdater batch) {
        return jooq.update(BID)
                .set(BID.STATE, Lost)
                .set(BID.UPDATED, Optional.of(now))
                .where(BID.TID.eq(tid),
                        BID.STATE.notIn(Quit, Lost, Expl),
                        BID.GID.eq(Optional.of(gid)),
                        BID.UID.notIn(winnerIds))
                .execute();
    }

    public void setGroupForUids(int gid, int tid, List<ParticipantMemState> groupBids) {
        groupBids.forEach(bid -> bid.setGid(Optional.of(gid)));
        jooq.update(BID)
                .set(BID.GID, Optional.of(gid))
                .where(BID.TID.eq(tid),
                        BID.UID.in(groupBids.stream()
                                .map(ParticipantMemState::getUid)
                                .map(Uid::getId)
                                .collect(Collectors.toList())))
                .execute();
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<BidState> getState(int tid, int uid) {
        return ofNullable(
                jooq.select(BID.STATE)
                        .from(BID)
                        .where(BID.TID.eq(tid), BID.UID.eq(uid))
                        .fetchOne())
                .map(Record1::value1);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void resign(int uid, Integer tid, BidState targetState, Instant now) {
        log.info("User {} leaves {} from tid {}",
                uid,
                jooq.update(BID)
                        .set(BID.STATE, targetState)
                        .set(BID.UPDATED, Optional.of(now))
                        .where(BID.UID.eq(uid), BID.TID.eq(tid))
                        .execute(),
                tid);
    }

    public void enlist(ParticipantMemState bid, Instant now, DbUpdater batch) {
        batch.exec(DbUpdate.builder()
                .logBefore(() -> log.info("User {} enlisted to tournament {}", bid.getUid(), bid.getTid()))
                .query(jooq.insertInto(BID, BID.CID, BID.TID, BID.UID, BID.STATE)
                        .values(bid.getCid(), bid.getTid().getTid(), bid.getUid().getId(), bid.getBidState())
                        .onDuplicateKeyUpdate()
                        .set(BID.STATE, bid.getBidState())
                        .set(BID.UPDATED, Optional.of(now))
                        .set(BID.CID, bid.getCid()))
                .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<ParticipantState> findEnlisted(int tid) {
        return jooq.select(BID.UID, USERS.NAME, BID.STATE, CATEGORY.NAME, CATEGORY.CID)
                .from(BID)
                .innerJoin(USERS)
                .on(BID.UID.eq(USERS.UID))
                .innerJoin(CATEGORY)
                .on(BID.CID.eq(CATEGORY.CID))
                .where(BID.TID.eq(tid))
                .fetch()
                .map(r -> ParticipantState.builder()
                        .category(CategoryInfo.builder()
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

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<DatedParticipantState> getParticipantInfo(int tid, int uid) {
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
                        .category(CategoryInfo.builder()
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

    public void setCategory(SetCategory setCategory, Instant now, DbUpdater batch) {
        batch.exec(DbUpdate.builder()
                .query(jooq.update(BID)
                        .set(BID.CID, setCategory.getCid())
                        .set(BID.UPDATED, Optional.of(now))
                        .where(BID.TID.eq(setCategory.getTid()),
                                BID.UID.eq(setCategory.getUid())))
                .build());
    }

    public void resetStateByTid(int tid, Instant now, DbUpdater batch) {
        batch.exec(jooq.update(BID)
                .set(BID.STATE, BidState.Want)
                .set(BID.UPDATED, Optional.of(now))
                .where(BID.TID.eq(tid), BID.STATE.ne(Quit)));
    }

    public Map<Integer, ParticipantMemState> loadParticipants(Tid tid) {
        return jooq.select(BID.UID, BID.STATE, USERS.NAME, BID.GID, BID.CID)
                .from(BID)
                .innerJoin(USERS).on(BID.UID.eq(USERS.UID))
                .where(BID.TID.eq(tid.getTid()))
                .fetch()
                .stream()
                .collect(toMap(r -> r.get(BID.UID),
                        r -> ParticipantMemState.builder()
                                .tid(tid)
                                .cid(r.get(BID.CID))
                                .gid(r.get(BID.GID))
                                .name(r.get(USERS.NAME))
                                .uid(new Uid(r.get(BID.UID)))
                                .bidState(r.get(BID.STATE))
                                .build()));
    }
}
