package org.dan.ping.pong.app.category;

import static ord.dan.ping.pong.jooq.Tables.USERS;
import static ord.dan.ping.pong.jooq.tables.Bid.BID;
import static ord.dan.ping.pong.jooq.tables.Category.CATEGORY;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.tournament.EnlistedCategory;
import org.dan.ping.pong.app.user.UserInfo;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

@Slf4j
public class CategoryDao {
    private static final String ENLISTED = "enlisted";
    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public int create(NewCategory newCategory) {
        final int cid = jooq.insertInto(CATEGORY, CATEGORY.NAME, CATEGORY.TID)
                .values(newCategory.getName(), newCategory.getTid())
                .returning(CATEGORY.CID)
                .fetchOne()
                .getCid();
        log.info("Set id {} to category {}", cid, newCategory);
        return cid;
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<CategoryInfo> listCategoriesByTid(int tid) {
        return jooq.select(CATEGORY.CID, CATEGORY.NAME)
                .from(CATEGORY)
                .where(CATEGORY.TID.eq(tid))
                .fetch()
                .map(r -> CategoryInfo.builder()
                        .cid(r.get(CATEGORY.CID))
                        .name(r.get(CATEGORY.NAME))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<UserInfo> listCategoryMembers(int cid) {
        return jooq.select(BID.UID, USERS.NAME)
                .from(BID)
                .innerJoin(USERS)
                .on(BID.UID.eq(USERS.UID))
                .where(BID.CID.eq(cid))
                .fetch()
                .map(r -> UserInfo.builder()
                        .uid(r.get(BID.UID))
                        .name(r.get(USERS.NAME))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<EnlistedCategory> listEnlistedInCategoriesByTid(int tid) {
        return jooq.select(CATEGORY.CID, CATEGORY.NAME, BID.UID.count().as(ENLISTED))
                .from(CATEGORY)
                .leftJoin(BID).on(CATEGORY.CID.eq(BID.CID))
                .where(CATEGORY.TID.eq(tid), BID.STATE.ne(BidState.Quit))
                .fetch()
                .map(r -> EnlistedCategory.builder()
                        .categoryInfo(CategoryInfo.builder()
                                .cid(r.get(CATEGORY.CID))
                                .name(r.get(CATEGORY.NAME))
                                .build())
                        .enlisted(r.get(BID.UID.as(ENLISTED)))
                        .build());
    }

    @Transactional(TRANSACTION_MANAGER)
    public void delete(int cid) {
        log.info("Delete category {}", cid);
        jooq.deleteFrom(CATEGORY)
                .where(CATEGORY.CID.eq(cid))
                .execute();
    }
}
