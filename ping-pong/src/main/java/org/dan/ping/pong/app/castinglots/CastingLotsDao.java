package org.dan.ping.pong.app.castinglots;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.log;
import static java.lang.Math.toRadians;
import static java.util.Optional.empty;
import static ord.dan.ping.pong.jooq.Tables.BID;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.match.MatchType.Grup;
import static org.dan.ping.pong.app.match.MatchType.POff;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import ord.dan.ping.pong.jooq.tables.Place;
import org.dan.ping.pong.app.bid.TournamentGroupingBid;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.score.MatchScoreDao;
import org.dan.ping.pong.app.tournament.DbUpdater;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class CastingLotsDao {
    @Inject
    private DSLContext jooq;

    @Inject
    private MatchDao matchDao;

    @Inject
    private MatchScoreDao matchScoreDao;

    public int generateGroupMatches(OpenTournamentMemState tournament, int gid, List<ParticipantMemState> groupBids) {
        final int tid = tournament.getTid();
        CastingLotsDao.log.info("Generate matches for group {} in tournament {}", gid, tid);
        int priorityGroup = 0;
        for (int i = 0; i < groupBids.size(); ++i) {
            final ParticipantMemState bid1 = groupBids.get(i);
            for (int j = i + 1; j < groupBids.size(); ++j) {
                final ParticipantMemState bid2 = groupBids.get(j);
                final int mid = matchDao.createGroupMatch(tid,
                        gid, bid1.getCid(), ++priorityGroup,
                        bid1.getUid().getId(), bid2.getUid().getId());
                tournament.getMatches().put(mid, MatchInfo.builder()
                        .tid(tid)
                        .mid(mid)
                        .state(MatchState.Place)
                        .gid(Optional.of(gid))
                        .participantIdScore(ImmutableMap.of(
                                bid1.getUid().getId(), new ArrayList<>(),
                                bid2.getUid().getId(), new ArrayList<>()))
                        .type(Grup)
                        .cid(tournament.getGroups().get(gid).getCid())
                        .build());
                CastingLotsDao.log.info("New match {} between {} and {}", mid,
                        bid1.getUid(), bid2.getUid());
            }
        }
        return priorityGroup;
    }

    @Transactional(TRANSACTION_MANAGER)
    public int generatePlayOffMatches(OpenTournamentMemState tinfo, Integer cid,
            int playOffStartPositions, int basePlayOffPriority) {
        final int tid = tinfo.getTid();
        CastingLotsDao.log.info("Generate play off matches for {} groups in tournament {}",
                playOffStartPositions, tid);
        if (playOffStartPositions == 1) {
            CastingLotsDao.log.info("Tournament {}:{} will be without play off", tid, cid);
            return 0;
        } else {
            checkArgument(playOffStartPositions > 0, "not enough groups %s", playOffStartPositions);
            checkArgument(playOffStartPositions % 2 == 0, "odd number groups %s", playOffStartPositions);
        }
        final int levels = (int) (log(playOffStartPositions) / log(2));
        final int lowestPriority = basePlayOffPriority + levels;
        return PlayOffGenerator.builder()
                .tid(tid)
                .cid(cid)
                .thirdPlaceMatch(tinfo.getRule().getPrizeWinningPlaces() > 2)
                .matchDao(matchDao)
                .build()
                .generateTree(levels, empty(), lowestPriority,
                        TypeChain.of(Gold, POff), empty()).get();
    }
}
