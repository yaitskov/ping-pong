package org.dan.ping.pong.app.tournament;

import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.cache.CacheLoader;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.category.CategoryInfo;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.score.MatchScoreDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;


public class TournamentCacheLoader extends CacheLoader<Tid, OpenTournamentMemState> {
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
    public OpenTournamentMemState load(Tid tid) throws Exception {
        final TournamentRow row = tournamentDao.getRow(tid).orElseThrow(() -> notFound("tournament " + tid));
        return OpenTournamentMemState.builder()
                .participants(bidDao.loadParticipants(tid))
                .categories(categoryDao.listCategoriesByTid(tid.getTid()).stream()
                        .collect(toMap(CategoryInfo::getCid, o -> o)))
                .groups(groupDao.load(tid))
                .matches(combineMatchesAndSets(matchDao.load(tid), matchScoreDao.load(tid)))
                .tid(tid.getTid())
                .adminIds(tournamentDao.loadAdmins(tid))
                .state(row.getState())
                .completeAt(row.getEndedAt())
                .pid(row.getPid())
                .rule(row.getRules())
                .build();
    }

    private Map<Integer, MatchInfo> combineMatchesAndSets(List<MatchInfo> matches,
            Map<Integer, Map<Integer, List<Integer>>> sets) {
        return matches.stream().collect(
                toMap(MatchInfo::getMid,
                        m -> {
                            m.setParticipantIdScore(sets.get(m.getMid()));
                            return m;
                        }));
    }
}
