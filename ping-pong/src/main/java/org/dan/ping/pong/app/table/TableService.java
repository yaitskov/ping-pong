package org.dan.ping.pong.app.table;

import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.PendingMatchInfo;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import javax.inject.Inject;

@Slf4j
public class TableService {
    @Inject
    private TableDao tableDao;
    @Inject
    private MatchDao matchDao;
    @Inject
    private BidDao bidDao;

    @Transactional(TRANSACTION_MANAGER)
    public int scheduleFreeTables(int tid, Instant now) {
        log.info("Schedule matches in tid {}", tid);
        final List<TableInfo> freeTables = tableDao.findFreeTables(tid);
        final List<PendingMatchInfo> matches = matchDao.selectForScheduling(
                Math.max(1, freeTables.size()), tid);
        log.info("Found {} free tables and {} matches for them",
                freeTables.size(), matches.size());
        final int tablesToAllocate = Math.min(freeTables.size(), matches.size());
        for (int i = 0; i < tablesToAllocate; ++i) {
            final int mid = matches.get(i).getMid();
            tableDao.locateMatch(freeTables.get(i).getTableId(), mid);
            matchDao.markAsSchedule(mid, now);
            bidDao.markParticipantsBusy(tid, matches.get(i).getUids());
        }
        if (freeTables.isEmpty()
                && !matches.isEmpty()
                && !tableDao.hasUsableTables(tid)) {
            throw badRequest("Tournament " + tid
                    + " doesn't have any table. "
                    + "Add tables to the place and schedule matches again.");
        }
        return tablesToAllocate;
    }

    public void freeTables(int tid) {
        tableDao.freeTablesByTid(tid);
    }

    public List<TableStatedLink> findByPlaceId(int placeId) {
        return tableDao.findByPlaceId(placeId);
    }

    public void setStatus(SetTableState update) {
        tableDao.setStatus(update);
    }

    public void create(CreateTables create) {
        tableDao.createTables(create.getPlaceId(), create.getQuantity());
    }
}
