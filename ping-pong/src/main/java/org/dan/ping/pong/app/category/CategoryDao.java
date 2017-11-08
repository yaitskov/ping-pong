package org.dan.ping.pong.app.category;

import static java.util.stream.Collectors.toList;
import static ord.dan.ping.pong.jooq.tables.Category.CATEGORY;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.tournament.Tid;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

@Slf4j
public class CategoryDao {
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
    public List<CategoryLink> listCategoriesByTid(Tid tid) {
        return jooq.select(CATEGORY.CID, CATEGORY.NAME)
                .from(CATEGORY)
                .where(CATEGORY.TID.eq(tid))
                .fetch()
                .map(r -> CategoryLink.builder()
                        .cid(r.get(CATEGORY.CID))
                        .name(r.get(CATEGORY.NAME))
                        .build());
    }

    @Transactional(TRANSACTION_MANAGER)
    public void delete(int cid) {
        log.info("Delete category {}", cid);
        jooq.deleteFrom(CATEGORY)
                .where(CATEGORY.CID.eq(cid))
                .execute();
    }

    @Transactional(TRANSACTION_MANAGER)
    public void copy(Tid originTid, Tid tid) {
        final List<CategoryLink> categories = listCategoriesByTid(originTid);
        jooq.batch(
                categories.stream().map(cinfo ->
                        jooq.insertInto(CATEGORY, CATEGORY.NAME, CATEGORY.TID)
                                .values(cinfo.getName(), tid))
                .collect(toList()))
                .execute();
    }
}
