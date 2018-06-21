package org.dan.ping.pong.app.category;

import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.jooq.tables.Category.CATEGORY;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.db.DbUpdateSql;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

@Slf4j
public class CategoryDaoMysql implements CategoryDao {
    @Inject
    private DSLContext jooq;

    @Override
    public void create(NewCategory newCategory, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.insertInto(
                        CATEGORY, CATEGORY.NAME,
                        CATEGORY.TID, CATEGORY.CID)
                        .values(newCategory.getName(),
                                newCategory.getTid(),
                                newCategory.getCid()))
                .build());
    }

    @Override
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

    @Override
    @Transactional(TRANSACTION_MANAGER)
    public void delete(Tid tid, int cid, DbUpdater batch) {
        log.info("Delete category {}", cid);
        jooq.deleteFrom(CATEGORY)
                .where(CATEGORY.CID.eq(cid))
                .execute();
    }

    @Override
    @Transactional(TRANSACTION_MANAGER)
    public void copy(Tid originTid, Tid tid) {
        final List<CategoryLink> categories = listCategoriesByTid(originTid);
        jooq.batch(
                categories.stream().map(cinfo ->
                        jooq.insertInto(CATEGORY, CATEGORY.NAME, CATEGORY.TID, CATEGORY.CID)
                                .values(cinfo.getName(), tid, cinfo.getCid()))
                .collect(toList()))
                .execute();
    }
}
