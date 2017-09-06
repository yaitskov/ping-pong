package org.dan.ping.pong.app.table;

import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.table.TableState.Busy;
import static org.dan.ping.pong.app.table.TableState.Free;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.place.PlaceMemState;
import org.dan.ping.pong.app.tournament.DbUpdater;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class TableService {
    @Inject
    private TableDao tableDao;
    @Inject
    private MatchDao matchDao;
    @Inject
    private BidDao bidDao;

    private List<TableInfo> findFreeTables(PlaceMemState place) {
        return findTablesByState(place, Free);
    }

    private List<TableInfo> findTablesByState(PlaceMemState place, TableState state) {
        return place.getTables().values().stream()
                .filter(tinfo -> tinfo.getState() == state)
                .collect(toList());
    }

    private List<MatchInfo> selectForScheduling(
            int matchesToSchedule, OpenTournamentMemState tournament) {
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getState() == Place)
                .filter(minfo -> minfo.getParticipantIdScore().size() == 2)
                .filter(minfo -> minfo.getParticipantIdScore().keySet().stream()
                        .map(uid -> tournament.getParticipants().get(uid).getBidState())
                        .allMatch(bidState -> bidState == BidState.Wait))
                .limit(matchesToSchedule)
                .collect(toList());
    }

    void freeTablesForCompleteMatches(OpenTournamentMemState tournament, PlaceMemState place, DbUpdater batch) {
        place.getTables().values().stream()
                .filter(t -> t.getMid().isPresent())
                .forEach(t -> {
                    final MatchInfo match = tournament.getMatchById(t.getMid().get());
                    switch (match.getState()) {
                        case Over:
                        case Auto:
                            freeTable(batch, t);
                            break;
                        case Draft:
                        case Place:
                            throw internalError("Unexpected state " + match.getState());
                        case Game:
                            // ok keep
                            break;
                        default:
                            throw internalError("Unknown state " + match.getState());
                    }
                });
    }

    @Transactional(TRANSACTION_MANAGER)
    public int scheduleFreeTables(OpenTournamentMemState tournament, PlaceMemState place,
            Instant now, DbUpdater batch) {
        log.info("Schedule matches in tid {}", tournament.getTid());
        freeTablesForCompleteMatches(tournament, place, batch);
        final List<TableInfo> freeTables = findFreeTables(place);
        final List<MatchInfo> matches = selectForScheduling(
                Math.max(1, freeTables.size()), tournament);
        log.info("Found {} free tables and {} matches for them",
                freeTables.size(), matches.size());
        final int tablesToAllocate = Math.min(freeTables.size(), matches.size());
        for (int i = 0; i < tablesToAllocate; ++i) {
            final MatchInfo match = matches.get(i);
            tableDao.locateMatch(freeTables.get(i), match.getMid(), batch);
            matchDao.markAsSchedule(match, now);
            bidDao.markParticipantsBusy(tournament, match.getUids(), now, batch);
        }
        if (freeTables.isEmpty()
                && !matches.isEmpty()
                && !hasUsableTables(place)) {
            throw badRequest("Tournament " + tournament.getTid()
                    + " doesn't have any table. "
                    + "Add tables to the place and schedule matches again.");
        }
        return tablesToAllocate;
    }

    private boolean hasUsableTables(PlaceMemState place) {
        return place.getTables().values().stream()
                .map(TableInfo::getState)
                .allMatch(state -> state == Free || state == Busy);
    }

    public void freeTables(PlaceMemState place, Set<Integer> mids, DbUpdater batch) {
        place.getTables().values().stream()
                .filter(table -> table.getMid().map(mids::contains).orElse(false))
                .forEach(table -> freeTable(batch, table));
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

    public void freeTable(PlaceMemState place, int mid, DbUpdater batch) {
        final TableInfo tableInfo = place.getTables().values().stream()
                .filter(tinfo -> tinfo.getMid().equals(Optional.of(mid)))
                .findAny()
                .orElseThrow(() -> internalError("Place "
                        + place.getName()
                        + " has no table allocated for the match "
                        + mid));
        freeTable(batch, tableInfo);
    }

    private void freeTable(DbUpdater batch, TableInfo tableInfo) {
        tableInfo.setMid(Optional.empty());
        tableInfo.setState(Free);
        tableDao.freeTable(tableInfo.getTableId(), batch);
    }
}
