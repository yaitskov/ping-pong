package org.dan.ping.pong.app.tournament;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.cache.CacheLoader;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.playoff.PowerRange;
import org.dan.ping.pong.app.score.MatchScoreDao;
import org.dan.ping.pong.util.collection.MaxValue;
import org.dan.ping.pong.util.counter.BidSeqGen;
import org.dan.ping.pong.util.counter.CidSeqGen;
import org.dan.ping.pong.util.counter.DidSeqGen;
import org.dan.ping.pong.util.counter.GidSeqGen;
import org.dan.ping.pong.util.counter.MidSeqGen;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;


@Slf4j
public class TournamentCacheLoader extends CacheLoader<Tid, TournamentMemState>  {
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
        final MaxValue<Mid> maxMid = new MaxValue<>(Mid.of(1));
        final MaxValue<Bid> maxBid = new MaxValue<>(Bid.of(1));
        final Map<Mid, MatchInfo> matchMap = combineMatchesAndSets(
                matchDao.load(tid, maxMid),
                matchScoreDao.load(tid));
        final MaxValue<Cid> maxCid = new MaxValue<>(Cid.of(1));
        final MaxValue<Gid> maxGid = new MaxValue<>(Gid.of(1));
        final Map<Bid, ParticipantMemState> participants = bidDao.loadParticipants(tid, maxBid);
        return TournamentMemState.builder()
                .name(row.getName())
                .condActions(OneTimeCondActions
                        .builder()
                        .onScheduleTables(new ArrayList<>())
                        .build())
                .powerRange(new PowerRange())
                .type(row.getType())
                .uidCid2Bid(participants.values().stream()
                        .collect(groupingBy(ParticipantMemState::getUid,
                                toMap(ParticipantMemState::getCid,
                                        ParticipantMemState::getBid))))
                .participants(participants)
                .categories(categoryDao.listCategoriesByTid(tid).stream()
                        .peek(c -> maxCid.accept(c.getCid()))
                        .collect(toMap(CategoryLink::getCid, o -> o)))
                .groups(groupDao.load(tid, maxGid))
                .nextCategory(new CidSeqGen(maxCid.getMax()))
                .nextGroup(new GidSeqGen(maxGid.getMax()))
                .nextDispute(new DidSeqGen(0)) // to be load
                .nextMatch(new MidSeqGen(maxMid.getMax()))
                .nextBid(new BidSeqGen(maxBid.getMax()))
                .matches(matchMap)
                .tid(tid)
                .sport(row.getSport())
                .adminIds(tournamentDao.loadAdmins(tid))
                .state(row.getState())
                .ticketPrice(row.getTicketPrice())
                .opensAt(row.getStartedAt())
                .previousTid(row.getPreviousTid())
                .completeAt(row.getEndedAt())
                .pid(row.getPid())
                .rule(row.getRules())
                .build();
    }

    private Map<Mid, MatchInfo> combineMatchesAndSets(List<MatchInfo> matches,
            Map<Mid, Map<Bid, List<Integer>>> sets) {
        return matches.stream().collect(
                toMap(MatchInfo::getMid,
                        m -> {
                            ofNullable(sets.get(m.getMid()))
                                    .ifPresent(psets -> psets.forEach((bid, usets) ->
                                            m.getParticipantIdScore().put(bid, usets)));
                            return m;
                        }));
    }
}
