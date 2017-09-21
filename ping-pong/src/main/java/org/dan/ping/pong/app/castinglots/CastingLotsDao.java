package org.dan.ping.pong.app.castinglots;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.log;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.BID;
import static org.dan.ping.pong.app.group.GroupSchedule.DEFAULT_SCHEDULE;
import static org.dan.ping.pong.app.match.MatchType.Gold;
import static org.dan.ping.pong.app.match.MatchType.Grup;
import static org.dan.ping.pong.app.match.MatchType.POff;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.castinglots.rank.OrderDirection;
import org.dan.ping.pong.app.group.GroupSchedule;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class CastingLotsDao {
    @Inject
    private DSLContext jooq;

    @Inject
    private MatchDao matchDao;

    private List<Integer> pickSchedule(OpenTournamentMemState tournament,
            List<ParticipantMemState> groupBids) {
        final GroupSchedule groupSchedules = tournament.getRule().getGroup().get()
                .getSchedule().orElse(DEFAULT_SCHEDULE);
        return ofNullable(groupSchedules.getSize2Schedule().get(groupBids.size()))
                .orElseGet(() -> ofNullable(DEFAULT_SCHEDULE.getSize2Schedule().get(groupBids.size()))
                        .orElseThrow(() -> internalError("No schedule for group of " + groupBids.size())));
    }

    public int generateGroupMatches(OpenTournamentMemState tournament, int gid,
            List<ParticipantMemState> groupBids, int priorityGroup) {
        final int tid = tournament.getTid();
        CastingLotsDao.log.info("Generate matches for group {} in tournament {}", gid, tid);
        final List<Integer> schedule = pickSchedule(tournament, groupBids);
        for (int i = 0; i < schedule.size();) {
            final int bidIdxA = schedule.get(i++);
            final int bidIdxB = schedule.get(i++);
            final ParticipantMemState bid1 = groupBids.get(bidIdxA);
            final ParticipantMemState bid2 = groupBids.get(bidIdxB);
            final int mid = matchDao.createGroupMatch(tid,
                    gid, bid1.getCid(), ++priorityGroup,
                    bid1.getUid().getId(), bid2.getUid().getId());
            tournament.getMatches().put(mid, MatchInfo.builder()
                    .tid(tid)
                    .mid(mid)
                    .priority(priorityGroup)
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
        return priorityGroup;
    }

    public int generatePlayOffMatches(OpenTournamentMemState tinfo, Integer cid,
            int playOffStartPositions, int basePlayOffPriority) {
        final int tid = tinfo.getTid();
        CastingLotsDao.log.info("Generate play off matches for {} bids in tid {}",
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
                .tournament(tinfo)
                .cid(cid)
                .thirdPlaceMatch(tinfo.getRule().getPlayOff().get().getThirdPlaceMatch() == 1)
                .matchDao(matchDao)
                .build()
                .generateTree(levels, empty(), lowestPriority,
                        TypeChain.of(Gold, POff), empty()).get();
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<Integer> loadRanks(Tid tid, Set<Integer> uids, OrderDirection direction) {
        return jooq.select(BID.UID).from(BID)
                .where(BID.TID.eq(tid.getTid()),
                        BID.UID.in(uids),
                        BID.PROVIDED_RANK.isNotNull())
                .orderBy(direction.setupOrder(BID.PROVIDED_RANK))
                .fetch()
                .map(r -> r.get(BID.UID));
    }
}
