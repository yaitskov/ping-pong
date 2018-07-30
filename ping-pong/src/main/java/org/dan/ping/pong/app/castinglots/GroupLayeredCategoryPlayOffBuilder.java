package org.dan.ping.pong.app.castinglots;

import static com.google.common.collect.ArrayListMultimap.create;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ArrayListMultimap;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

@Slf4j
public abstract class GroupLayeredCategoryPlayOffBuilder {
    @Inject
    protected GroupService groupService;

    protected Map<Integer, List<ParticipantMemState>> findFinalPositions(
            TournamentMemState conTour, TournamentMemState mTour,
            List<ParticipantMemState> bids) {
        final Cid masterCid = mTour.getParticipant(bids.get(0).getBid()).getCid();
        final List<GroupInfo> orderedGroups = mTour.getGroupsByCategory(masterCid);
        orderedGroups.sort(comparing(GroupInfo::getOrdNumber));
        final ArrayListMultimap<Integer, Bid> bidsByFinalGroupPosition = create();
        final int quits = mTour.groupRules().getQuits();
        orderedGroups.forEach(groupInfo -> {
            final List<Bid> orderedBids = groupService.orderBidsInGroup(
                    groupInfo.getGid(), mTour,
                    groupService.findAllMatchesInGroup(mTour, groupInfo.getGid()));
            for (int i = quits; i < orderedBids.size(); ++i) {
                bidsByFinalGroupPosition.put(i, orderedBids.get(i));
            }
        });
        log.info("Found {} layers in {} groups in cid {}",
                bidsByFinalGroupPosition.keySet().size(),
                orderedGroups.size(), masterCid);

        return bidsByFinalGroupPosition.keySet().stream()
                .collect(toMap(
                        level -> level,
                        level -> bidsByFinalGroupPosition.get(level).stream()
                                .map(conTour::getBid)
                                .filter(Objects::nonNull) // exclude master bid
                                .collect(toList())));
    }
}
