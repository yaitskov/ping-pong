package org.dan.ping.pong.app.category;

import org.dan.ping.pong.app.tournament.Tid;

import java.util.List;

public interface CategoryDao {
    int create(NewCategory newCategory);

    List<CategoryLink> listCategoriesByTid(Tid tid);

    void delete(int cid);

    void copy(Tid originTid, Tid tid);
}
