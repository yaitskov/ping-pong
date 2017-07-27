package org.dan.ping.pong.app.bid;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.BID;
import static ord.dan.ping.pong.jooq.Tables.CATEGORY;
import static ord.dan.ping.pong.jooq.Tables.USERS;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.category.CategoryInfo;
import org.dan.ping.pong.app.tournament.EnlistTournament;
import org.dan.ping.pong.app.user.UserLink;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Slf4j
public class BidDao {
    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public void setBidState(BidId bid, List<BidState> expected, BidState state) {
        log.info("Set bid status {} for {} if {}", state, bid, expected);
        if (0 == jooq.update(BID).set(BID.STATE, state)
                .where(BID.UID.eq(bid.getUid()),
                        BID.TID.eq(bid.getTid()),
                        BID.STATE.in(expected))
                .execute()) {
            throw badRequest("Participant status was not " + expected);
        }
    }

    @Transactional(TRANSACTION_MANAGER)
    public void setBidState(int tid, int uid, BidState expected, BidState state) {
        setBidState(BidId.builder().tid(tid).uid(uid).build(),
                singletonList(expected), state);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void casByTid(BidState expected, BidState set, int tid) {
        log.info("Put all {} bids of tid {} into {} state", expected, tid, set);
        if (0 == jooq.update(BID)
                .set(BID.STATE, set)
                .where(BID.TID.eq(tid), BID.STATE.eq(expected))
                .execute()) {
            throw badRequest("Tournament " + tid + " has no participants");
        }
    }

    @Transactional(TRANSACTION_MANAGER)
    public void markParticipantsBusy(int tid, List<Integer> uids) {
        if (2 != jooq.update(BID)
                .set(BID.STATE, Play)
                .where(BID.TID.eq(tid),
                        BID.UID.in(uids),
                        BID.STATE.eq(Wait))
                .execute()) {
            throw internalError("One of uids " + uids + " was busy");
        }
    }

    public int setStatesAfterGroup(Integer gid, int tid, Set<Integer> winnerIds) {
        return jooq.update(BID)
                .set(BID.STATE, Lost)
                .where(BID.TID.eq(tid),
                        BID.GID.eq(Optional.of(gid)),
                        BID.UID.notIn(winnerIds))
                .execute();
    }

    public void setGroupForUids(int gid, int tid, List<TournamentBid> groupBids) {
        if (groupBids.size() != jooq.update(BID)
                .set(BID.GID, Optional.of(gid))
                .where(BID.TID.eq(tid),
                        BID.UID.in(groupBids.stream()
                                .map(TournamentBid::getUid)
                                .collect(Collectors.toList())))
                .execute()) {
            throw internalError("Not all bids got group " + gid);
        }
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
    public void resign(int uid, Integer tid) {
        log.info("User {} resigned {} from tid {}",
                uid,
                jooq.update(BID)
                        .set(BID.STATE, BidState.Quit)
                        .where(BID.UID.eq(uid), BID.TID.eq(tid))
                        .execute(),
                tid);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void enlist(int uid, EnlistTournament enlistment) {
        log.info("User {} enlisted to tournament {}", uid, enlistment.getTid());
        jooq.insertInto(BID, BID.CID, BID.TID, BID.UID, BID.STATE)
                .values(enlistment.getCategoryId(), enlistment.getTid(), uid, Want)
                .onDuplicateKeyUpdate()
                .set(BID.STATE, Want)
                .set(BID.CID, enlistment.getCategoryId())
                .execute();
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

    public void setCategory(SetCategory setCategory) {
        jooq.update(BID).set(BID.CID, setCategory.getCid())
                .where(BID.TID.eq(setCategory.getTid()),
                        BID.UID.eq(setCategory.getUid()))
                .execute();
    }
}
