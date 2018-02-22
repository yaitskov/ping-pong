package org.dan.ping.pong.app.castinglots;

import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.castinglots.FlatCategoryPlayOffBuilder.validateBidsNumberInACategory;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.group.OrderUidsInGroupCmd;
import org.dan.ping.pong.app.match.MatchTag;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.jooq.tables.Bid;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class LayeredCategoryPlayOffBuilder implements CategoryPlayOffBuilder {
    @Inject
    private FlatCategoryPlayOffBuilder flatCategoryPlayOffBuilder;

    @Inject
    @Named(TOURNAMENT_CACHE)
    private LoadingCache<Tid, TournamentMemState> tournamentCache;

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> tournamentRelationCache;

    @Inject
    private GroupService groupService;

    @SneakyThrows
    public void build(TournamentMemState tournament, Integer cid,
            List<ParticipantMemState> bids, DbUpdater batch) {
        log.info("Layered console casting for tid {} cid {}", tournament.getTid(), cid);
        validateBidsNumberInACategory(bids);

        final RelatedTids relatedTids = tournamentRelationCache.get(tournament.getTid());
        final Tid masterTid = relatedTids.getParent().orElseThrow(() -> internalError("tid "
                + tournament.getTid() + " has no master tournament"));

        final TournamentMemState masterTournament = tournamentCache.get(masterTid);

        final int masterCid = masterTournament.getParticipant(bids.get(0).getUid()).getCid();
        final List<GroupInfo> orderedGroups = masterTournament.getGroupsByCategory(masterCid);
        orderedGroups.sort(Comparator.comparing(GroupInfo::getOrdNumber));
        final ArrayListMultimap<Integer, Uid> bidsByFinalGroupPosition = ArrayListMultimap.create();

        orderedGroups.forEach(groupInfo -> {
            final List<Uid> orderedUids = new OrderUidsInGroupCmd(masterTournament,
                    groupInfo.getGid(), groupService).getFinalUidsOrder();
            for (int i = 0; i < orderedUids.size(); ++i) {
                bidsByFinalGroupPosition.put(i, orderedUids.get(i));
            }
        });
        log.info("Found {} layers in {} groups in cid {}",
                bidsByFinalGroupPosition.keySet().size(),
                orderedGroups.size(), masterCid);
        for (int i = masterTournament.getRule().getGroup()
                .orElseThrow(() -> internalError("Tournament " + masterTournament.getTid()
                        + " has no groups")).getQuits();
             i < bidsByFinalGroupPosition.keySet().size(); ++i) {
            final List<ParticipantMemState> bidsInTag = bidsByFinalGroupPosition.get(i).stream()
                    .map(tournament::getBid)
                    .filter(Objects::nonNull) // exclude master bid
                    .collect(toList());
            flatCategoryPlayOffBuilder.build(tournament, cid,
                    bidsInTag, batch,
                    MatchTag.builder().prefix("L").number(i).build());
        }
    }
}
