package org.dan.ping.pong.app.server.table;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.server.match.MatchState.Place;
import static org.dan.ping.pong.app.server.table.TableState.Busy;
import static org.dan.ping.pong.app.server.table.TableState.Free;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.server.bid.BidDao;
import org.dan.ping.pong.app.server.bid.BidState;
import org.dan.ping.pong.app.server.match.MatchDao;
import org.dan.ping.pong.app.server.match.MatchInfo;
import org.dan.ping.pong.app.server.match.MatchService;
import org.dan.ping.pong.app.server.place.PlaceDao;
import org.dan.ping.pong.app.server.place.PlaceMemState;
import org.dan.ping.pong.app.server.tournament.DbUpdater;
import org.dan.ping.pong.app.server.tournament.OpenTournamentMemState;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class TableService {
    public static final String STATE = "state";

    @Inject
    private TableDao tableDao;
    @Inject
    private MatchDao matchDao;
    @Inject
    private BidDao bidDao;
    @Inject
    private MatchService matchService;

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
        final Set<Integer> pickedUids = new HashSet<>();
        return tournament.getMatches().values().stream()
                .filter(minfo -> minfo.getState() == Place)
                .filter(minfo -> minfo.getParticipantIdScore().size() == 2)
                .filter(minfo -> minfo.getParticipantIdScore().keySet().stream()
                        .map(uid -> tournament.getParticipants().get(uid).getBidState())
                        .allMatch(bidState -> bidState == BidState.Wait))
                .sorted(comparingInt(MatchInfo::getPriority)
                        .thenComparingInt(MatchInfo::getMid))
                .collect(toList())
                .stream()
                .filter(minfo -> minfo.getParticipantIdScore().keySet().stream()
                         .noneMatch(pickedUids::contains))
                .map(minfo -> {
                    minfo.getParticipantIdScore().keySet().forEach(pickedUids::add);
                    return minfo;
                })
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
                            throw internalError("unexpected-match-state", STATE, match.getState());
                        case Game:
                            // ok keep
                            break;
                        default:
                            throw internalError("unknown-match-state", STATE, match.getState());
                    }
                });
    }

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
            matchService.markAsSchedule(match, now, batch);
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
        return !place.getTables().isEmpty() && place.getTables().values().stream()
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

    public void setStatus(PlaceMemState place, SetTableState update, DbUpdater batch) {
        TableInfo table = place.getTable(update.getTableId());
        if (table.getMid().isPresent()) {
            throw badRequest("Table " + table.getTableId()
                    + " is used by match " + table.getMid());
        }
        tableDao.setStatus(update, batch);
    }

    public void create(CreateTables create) {
        tableDao.createTables(create.getPlaceId(), create.getQuantity());
    }

    private void freeTable(DbUpdater batch, TableInfo tableInfo) {
        tableInfo.setMid(Optional.empty());
        tableInfo.setState(Free);
        tableDao.freeTable(tableInfo.getTableId(), batch);
    }

    @Inject
    private PlaceDao placeDao;

    public void bindPlace(PlaceMemState place, DbUpdater batch, Optional<Integer> tid) {
        log.info("Rebind  pid {} from tid {} to {}",
                place.getPid(), place.getHostingTid(), tid);
        if (place.getHostingTid().equals(tid)) {
            return;
        }
        place.setHostingTid(tid);
        placeDao.setHostingTid(place, batch);
    }
}
