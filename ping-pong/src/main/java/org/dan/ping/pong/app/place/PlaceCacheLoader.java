package org.dan.ping.pong.app.place;

import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.cache.CacheLoader;
import org.dan.ping.pong.app.table.TableDao;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

public class PlaceCacheLoader extends CacheLoader<Pid, PlaceMemState> {
    @Inject
    private PlaceDao placeDao;

    @Inject
    private TableDao tableDao;

    @Override
    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public PlaceMemState load(Pid pid) throws Exception {
        final PlaceMemState result = placeDao.load(pid).orElseThrow(() -> notFound("Place " + pid));
        result.setTables(tableDao.load(pid));
        return result;
    }
}
