package org.dan.ping.pong.app.category;

import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.jooq.tables.Category.CATEGORY;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.db.DbUpdate;
import org.dan.ping.pong.sys.db.DbUpdateSql;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
                        CATEGORY.TID, CATEGORY.CID, CATEGORY.STATE)
                                .values(newCategory.getName(),
                                        newCategory.getTid(),
                                        newCategory.getCid(),
                                        CategoryState.Drt))
                .build());
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<CategoryMemState> listCategoriesByTid(Tid tid) {
        return jooq.select(CATEGORY.CID, CATEGORY.NAME, CATEGORY.STATE)
                .from(CATEGORY)
                .where(CATEGORY.TID.eq(tid))
                .fetch()
                .map(r -> CategoryMemState.builder()
                        .cid(r.get(CATEGORY.CID))
                        .state(r.get(CATEGORY.STATE))
                        .name(r.get(CATEGORY.NAME))
                        .build());
    }

    @Override
    public void delete(Tid tid, Cid cid, DbUpdater batch) {
        log.info("Delete category {}", cid);
        batch.exec(DbUpdateSql
                .builder()
                .query(jooq.deleteFrom(CATEGORY)
                        .where(CATEGORY.TID.eq(tid), CATEGORY.CID.eq(cid)))
                .mustAffectRows(Optional.of(1))
                .logBefore(() -> log.info("Remove category {} {}", tid, cid))
                .build());
    }

    @Override
    @Transactional(TRANSACTION_MANAGER)
    public void copy(Tid originTid, Tid tid) {
        final List<CategoryMemState> categories = listCategoriesByTid(originTid);
        jooq.batch(
                categories.stream().map(cat ->
                        jooq.insertInto(
                                CATEGORY, CATEGORY.NAME, CATEGORY.TID,
                                CATEGORY.CID, CATEGORY.STATE)
                                .values(
                                        cat.getName(), tid, cat.getCid(), cat.getState()))
                .collect(toList()))
                .execute();
    }

    @Override
    public void setState(Tid tid, Cid cid, CategoryState oldSt, CategoryState targetSt, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .query(jooq.update(CATEGORY)
                        .set(CATEGORY.STATE, targetSt)
                        .where(CATEGORY.TID.eq(tid), CATEGORY.CID.eq(cid)))
                .mustAffectRows(Optional.of(1))
                .build());
    }
}
