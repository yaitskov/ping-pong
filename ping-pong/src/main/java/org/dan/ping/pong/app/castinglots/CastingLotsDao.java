package org.dan.ping.pong.app.castinglots;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.log;
import static java.util.Optional.empty;
import static org.dan.ping.pong.app.castinglots.PlayOffGenerator.MID0;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.match.MatchType.POff;
import static org.dan.ping.pong.jooq.Tables.BID;
import static org.dan.ping.pong.jooq.Tables.USERS;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.ParticipantLink;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.rank.OrderDirection;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.match.MatchDaoServer;
import org.dan.ping.pong.app.match.MatchTag;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.playoff.PlayOffRule;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.jooq.tables.records.BidRecord;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.SortField;
import org.jooq.TableField;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class CastingLotsDao implements CastingLotsDaoIf {
    @Inject
    private DSLContext jooq;

    @Inject
    private MatchDaoServer matchDao;

    public Mid generatePlayOffMatches(DbUpdater batch, TournamentMemState tInfo, Cid cid,
            int playOffStartPositions, int basePlayOffPriority) {
        return generatePlayOffMatches(batch, tInfo, cid,
                playOffStartPositions, basePlayOffPriority, empty());
    }

    public Mid generatePlayOffMatches(DbUpdater batch, TournamentMemState tInfo, Cid cid,
            int playOffStartPositions, int basePlayOffPriority, Optional<MatchTag> tag) {
        final Tid tid = tInfo.getTid();
        log.info("Generate play off matches for {} bids in tid {}",
                playOffStartPositions, tid);
        if (playOffStartPositions == 1) {
            log.info("Tournament {}:{} will be without play off", tid, cid);
            return MID0;
        } else {
            checkArgument(playOffStartPositions > 0, "not enough groups %s",
                    playOffStartPositions);
            checkArgument(playOffStartPositions % 2 == 0, "odd number groups %s",
                    playOffStartPositions);
        }
        final int levels = (int) (log(playOffStartPositions) / log(2));
        final int lowestPriority = basePlayOffPriority + levels;
        final PlayOffRule playOffRule = tInfo.getRule().getPlayOff()
                .orElseThrow(() -> internalError("no play off rule in " + tid));
        final PlayOffGenerator generator = PlayOffGenerator.builder()
                .tournament(tInfo)
                .cid(cid)
                .batch(batch)
                .thirdPlaceMatch(playOffRule.getThirdPlaceMatch() == 1)
                .matchDao(matchDao)
                .tag(tag)
                .build();
        switch (playOffRule.getLosings()) {
            case 1:
                return generator.generateTree(levels, empty(), lowestPriority,
                        TypeChain.of(Gold, POff), empty()).get();
            case 2:
                return generator.generate2LossTree(2 * levels, lowestPriority).get();
            default:
                throw internalError("unsupported number of losings "
                        + playOffRule.getLosings() + " in " + tid + " ");
        }
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<Uid> loadRanks(Tid tid, Set<Uid> uids, OrderDirection direction) {
        return jooq.select(BID.UID).from(BID)
                .where(BID.TID.eq(tid),
                        BID.UID.in(uids),
                        BID.PROVIDED_RANK.isNotNull())
                .orderBy(setupOrder( direction, BID.PROVIDED_RANK))
                .fetch()
                .map(r -> r.get(BID.UID));
    }

    private SortField<Optional<Integer>> setupOrder(
            OrderDirection direction,
            TableField<BidRecord, Optional<Integer>> field) {
        switch (direction) {
            case Decrease:
                return field.desc();
            case Increase:
                return field.asc();
            default:
                throw internalError("unknown direction " + direction);
        }
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<Uid> loadSeed(Tid tid, Set<Uid> uids) {
        return jooq.select(BID.UID).from(BID)
                .where(BID.TID.eq(tid),
                        BID.UID.in(uids),
                        BID.SEED.isNotNull())
                .fetch()
                .map(r -> r.get(BID.UID));
    }

    @Transactional(TRANSACTION_MANAGER)
    public void orderCategoryBidsManually(OrderCategoryBidsManually order) {
        final List<Query> batch = new ArrayList();
        batch.add(jooq.update(BID).set(BID.SEED, Optional.empty())
                .where(BID.TID.eq(order.getTid()),
                        BID.CID.eq(order.getCid())));
        for (Bid bid : order.getBids()) {
             batch.add(jooq.update(BID)
                     .set(BID.SEED, Optional.of(batch.size()))
                     .where(BID.TID.eq(order.getTid()), BID.BID_.eq(bid)));
        }
        jooq.batch(batch).execute();
        final List<Uid> unseededUids = jooq.select(BID.UID).from(BID)
                .where(BID.TID.eq(order.getTid()),
                        BID.CID.eq(order.getCid()),
                        BID.SEED.isNull())
                .fetch()
                .map(r -> r.get(BID.UID));
        if (unseededUids.isEmpty()) {
            return;
        }
        throw badRequest("unseeded-uids-in-cid",
                ImmutableMap.of("cid", order.getCid(), "uids", unseededUids));
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<RankedBid> loadManualBidsOrder(Tid tid, Cid cid) {
        return jooq.select(USERS.NAME, BID.BID_, BID.PROVIDED_RANK, BID.SEED)
                .from(BID)
                .innerJoin(USERS)
                .on(BID.UID.eq(USERS.UID))
                .where(BID.TID.eq(tid), BID.CID.eq(cid))
                .orderBy(BID.SEED)
                .fetch()
                .map(r -> RankedBid.builder()
                        .user(ParticipantLink.builder()
                                .name(r.get(USERS.NAME))
                                .bid(r.get(BID.BID_))
                                .build())
                        .providedRank(r.get(BID.PROVIDED_RANK))
                        .seed(r.get(BID.SEED))
                        .build());
    }
}
