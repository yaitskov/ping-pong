package org.dan.ping.pong.app.category;

import static ord.dan.ping.pong.jooq.tables.Category.CATEGORY;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
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

    @Transactional(TRANSACTION_MANAGER)
    public void delete(int cid) {
        log.info("Delete category {}", cid);
        jooq.deleteFrom(CATEGORY)
                .where(CATEGORY.CID.eq(cid))
                .execute();
    }
}
