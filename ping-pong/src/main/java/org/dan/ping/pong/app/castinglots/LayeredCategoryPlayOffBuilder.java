package org.dan.ping.pong.app.castinglots;

import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.castinglots.FlatCategoryPlayOffBuilder.validateBidsNumberInACategory;
import static org.dan.ping.pong.app.match.MatchTag.CONSOLE_LEVEL;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.MatchTag;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public void build(TournamentMemState tournament, Cid cid,
            List<ParticipantMemState> bids, DbUpdater batch) {
        log.info("Layered console casting for tid {} cid {}", tournament.getTid(), cid);
        validateBidsNumberInACategory(bids);

        final RelatedTids relatedTids = tournamentRelationCache.get(tournament.getTid());
        final Tid masterTid = relatedTids.getParent().orElseThrow(() -> internalError("tid "
                + tournament.getTid() + " has no master tournament"));

        final TournamentMemState masterTournament = tournamentCache.get(masterTid);

        final Cid masterCid = masterTournament.getParticipant(bids.get(0).getBid()).getCid();
        final List<GroupInfo> orderedGroups = masterTournament.getGroupsByCategory(masterCid);
        orderedGroups.sort(Comparator.comparing(GroupInfo::getOrdNumber));
        final ArrayListMultimap<Integer, Bid> bidsByFinalGroupPosition = ArrayListMultimap.create();

        orderedGroups.forEach(groupInfo -> {
            final List<Bid> orderedBids = groupService.orderBidsInGroup(groupInfo.getGid(), masterTournament,
                    groupService.findAllMatchesInGroup(masterTournament, groupInfo.getGid()));
            for (int i = 0; i < orderedBids.size(); ++i) {
                bidsByFinalGroupPosition.put(i, orderedBids.get(i));
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
                    Optional.of(MatchTag.builder().prefix(CONSOLE_LEVEL).number(i).build()));
        }
    }
}
