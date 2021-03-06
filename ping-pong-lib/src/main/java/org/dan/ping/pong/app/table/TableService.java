package org.dan.ping.pong.app.table;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.table.TableState.Busy;
import static org.dan.ping.pong.app.table.TableState.Free;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.place.PlaceDao;
import org.dan.ping.pong.app.place.PlaceMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.TournamentCache;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.TournamentTerminator;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class TableService {
    public static final String STATE = "state";

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
            int matchesToSchedule, TournamentMemState tournament) {
        final Set<Bid> pickedBids = new HashSet<>();
        return tournament.getMatches().values().stream()
                .filter(mInfo -> mInfo.getState() == Place)
                .filter(mInfo -> mInfo.getParticipantIdScore().size() == 2)
                .filter(mInfo -> mInfo.getParticipantIdScore().keySet().stream()
                        .map(uid -> tournament.getParticipants().get(uid).getBidState())
                        .allMatch(bidState -> bidState == BidState.Wait))
                .sorted(comparingInt(MatchInfo::getPriority)
                        .thenComparing(MatchInfo::getMid))
                .collect(toList())
                .stream()
                .filter(mInfo -> mInfo.getParticipantIdScore().keySet().stream()
                         .noneMatch(pickedBids::contains))
                .map(mInfo -> {
                    mInfo.getParticipantIdScore().keySet()
                            .stream()
                            .map(tournament::getParticipant)
                            .map(ParticipantMemState::getUid)
                            .flatMap(uid -> tournament.findBidsByUid(uid).stream())
                            .forEach(pickedBids::add);
                    return mInfo;
                })
                .limit(matchesToSchedule)
                .collect(toList());
    }

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> tournamentRelatedCache;

    @Inject
    private TournamentCache tournamentCache;

    @SneakyThrows
    void freeTablesForCompleteMatches(TournamentMemState tournament, PlaceMemState place, DbUpdater batch) {
        final Set<Mid> parentMatches = tournamentRelatedCache.get(tournament.getTid()).getParent()
                .map(ptid -> tournamentCache.load(ptid))
                .map(parentTournament -> parentTournament.getMatches().keySet())
                .orElseGet(Collections::emptySet);
        place.getTables().values().stream()
                .filter(t -> t.getMid()/*.filter(mid -> !parentMatches.contains(mid))*/.isPresent())
                .forEach(t -> {
                    final MatchInfo match = tournament.getMatchById(t.getMid().get());
                    switch (match.getState()) {
                        case Over:
                        case Auto:
                            freeTable(batch, t);
                            break;
                        case Draft:
                        case Place:
                            if (t.getState() == Busy) {
                                freeTable(batch, t);
                            }
                            break;
                        case Game:
                            // ok keep
                            break;
                        default:
                            throw internalError("unknown-match-state", STATE, match.getState());
                    }
                });
    }

    public void markAsSchedule(MatchInfo match, Instant now, DbUpdater batch) {
        match.setState(Game);
        match.setStartedAt(Optional.of(now));
        matchDao.markAsSchedule(match, batch);
    }

    @Inject
    private TournamentTerminator tournamentTerminator;

    public int scheduleFreeTables(TournamentMemState tournament, PlaceMemState place,
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
            markAsSchedule(match, now, batch);
            bidDao.markParticipantsBusy(tournament, match.bids(), now, batch);
        }
        if (freeTables.isEmpty()
                && !matches.isEmpty()
                && !hasUsableTables(place)) {
            throw badRequest("Tournament " + tournament.getTid()
                    + " doesn't have any table. "
                    + "Add tables to the place and schedule matches again.");
        }
        if (matches.isEmpty()) {
            tournamentTerminator.endOfTournamentCategory(tournament, null, batch);
        }
        return tablesToAllocate;
    }

    private boolean hasUsableTables(PlaceMemState place) {
        return !place.getTables().isEmpty() && place.getTables().values().stream()
                .map(TableInfo::getState)
                .allMatch(state -> state == Free || state == Busy);
    }

    public void freeTables(PlaceMemState place, Set<Mid> mids, DbUpdater batch) {
        place.getTables().values().stream()
                .filter(table -> table.getMid().map(mids::contains).orElse(false))
                .forEach(table -> freeTable(batch, table));
    }

    public List<TableStatedLink> findByPlaceId(Pid placeId) {
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

    public void bindPlace(PlaceMemState place, DbUpdater batch, Optional<Tid> tid) {
        log.info("Rebind  pid {} from tid {} to {}",
                place.getPid(), place.getHostingTid(), tid);
        if (place.getHostingTid().equals(tid)) {
            return;
        }
        place.setHostingTid(tid);
        placeDao.setHostingTid(place, batch);
    }
}
