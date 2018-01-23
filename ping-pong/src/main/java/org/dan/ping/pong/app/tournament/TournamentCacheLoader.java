package org.dan.ping.pong.app.tournament;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.cache.CacheLoader;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.score.MatchScoreDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;


@Slf4j
public class TournamentCacheLoader extends CacheLoader<Tid, TournamentMemState> {
    public static final String TOURNAMENT_NOT_FOUND = "tournament not found";

    @Inject
    private TournamentDao tournamentDao;

    @Inject
    private BidDao bidDao;

    @Inject
    private MatchDao matchDao;

    @Inject
    private MatchScoreDao matchScoreDao;

    @Inject
    private GroupDao groupDao;

    @Inject
    private CategoryDao categoryDao;

    @Override
    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public TournamentMemState load(Tid tid) throws Exception {
        log.info("Loading tournament {}", tid);
        final TournamentRow row = tournamentDao.getRow(tid)
                .orElseThrow(() -> notFound(TOURNAMENT_NOT_FOUND, TID, tid));
        return TournamentMemState.builder()
                .name(row.getName())
                .type(row.getType())
                .participants(bidDao.loadParticipants(tid))
                .categories(categoryDao.listCategoriesByTid(tid).stream()
                        .collect(toMap(CategoryLink::getCid, o -> o)))
                .groups(groupDao.load(tid))
                .matches(combineMatchesAndSets(matchDao.load(tid), matchScoreDao.load(tid)))
                .tid(tid)
                .sport(row.getSport())
                .adminIds(tournamentDao.loadAdmins(tid))
                .state(row.getState())
                .ticketPrice(row.getTicketPrice())
                .opensAt(row.getStartedAt())
                .previousTid(row.getPreviousTid())
                .completeAt(row.getEndedAt())
                .pid(row.getPid())
                .consoleTid(row.getConsoleTid())
                .masterTid(row.getMasterTid())
                .rule(row.getRules())
                .build();
    }

    private Map<Mid, MatchInfo> combineMatchesAndSets(List<MatchInfo> matches,
            Map<Mid, Map<Uid, List<Integer>>> sets) {
        return matches.stream().collect(
                toMap(MatchInfo::getMid,
                        m -> {
                            ofNullable(sets.get(m.getMid()))
                                    .ifPresent(psets -> psets.forEach((uid, usets) ->
                                            m.getParticipantIdScore().put(uid, usets)));
                            return m;
                        }));
    }
}
