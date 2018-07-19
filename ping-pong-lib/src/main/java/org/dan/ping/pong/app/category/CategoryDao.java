package org.dan.ping.pong.app.category;

import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;

public interface CategoryDao {
    void create(NewCategory newCategory, DbUpdater batch);

    List<CategoryMemState> listCategoriesByTid(Tid tid);

    void delete(Tid tid, Cid cid, DbUpdater batch);

    void copy(Tid originTid, Tid tid);
}
