package org.dan.ping.pong.app.castinglots;

import static com.google.common.collect.ArrayListMultimap.create;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.castinglots.FlatCategoryPlayOffBuilder.validateBidsNumberInACategory;
import static org.dan.ping.pong.app.match.MatchTag.consoleTagO;
import static org.dan.ping.pong.app.match.MatchType.Gold;

import com.google.common.collect.ArrayListMultimap;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.MatchTag;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.rel.RelatedTournamentsService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class GroupLayeredCategoryPlayOffBuilder implements CategoryPlayOffBuilder {
    @Inject
    private FlatCategoryPlayOffBuilder flatCategoryPlayOffBuilder;

    @Inject
    private RelatedTournamentsService relatedTournaments;

    @Inject
    private GroupService groupService;

    @Inject
    private CastingLotsService castingLotsService;

    public void build(SelectedCid sCid, List<ParticipantMemState> bids) {
        validateBidsNumberInACategory(bids);
        final TournamentMemState masterTournament = relatedTournaments.findParent(sCid.tid());

        final Cid masterCid = masterTournament.getParticipant(bids.get(0).getBid()).getCid();
        final List<GroupInfo> orderedGroups = masterTournament.getGroupsByCategory(masterCid);
        orderedGroups.sort(Comparator.comparing(GroupInfo::getOrdNumber));
        final ArrayListMultimap<Integer, Bid> bidsByFinalGroupPosition = create();

        orderedGroups.forEach(groupInfo -> {
            final List<Bid> orderedBids = groupService.orderBidsInGroup(
                    groupInfo.getGid(), masterTournament,
                    groupService.findAllMatchesInGroup(masterTournament, groupInfo.getGid()));
            for (int i = 0; i < orderedBids.size(); ++i) {
                bidsByFinalGroupPosition.put(i, orderedBids.get(i));
            }
        });
        log.info("Found {} layers in {} groups in cid {}",
                bidsByFinalGroupPosition.keySet().size(),
                orderedGroups.size(), masterCid);

        for (int i = masterTournament.groupRules().getQuits();
             i < bidsByFinalGroupPosition.keySet().size(); ++i) {
            final List<ParticipantMemState> bidsInTag = bidsByFinalGroupPosition.get(i)
                    .stream()
                    .map(sCid.tournament()::getBid)
                    .filter(Objects::nonNull) // exclude master bid
                    .collect(toList());
            final Optional<MatchTag> tag = consoleTagO(i);
            final PlayOffGenerator generator = castingLotsService
                    .createPlayOffGen(sCid, tag, 0, Gold);
            flatCategoryPlayOffBuilder.build(bidsInTag, Optional.empty(), 0, generator);
        }
    }
}
