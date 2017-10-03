package org.dan.ping.pong.app.server.castinglots;

import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.server.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.server.group.GroupRules;
import org.dan.ping.pong.app.server.group.GroupSizeCalculator;
import org.dan.ping.pong.app.server.tournament.ParticipantMemState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

@Slf4j
public class GroupDivider {
    @Inject
    private GroupSizeCalculator groupSizeCalculator;

    public Map<Integer, List<ParticipantMemState>> divide(
            CastingLotsRule casting,
            GroupRules grouping,
            List<ParticipantMemState> bids) {
        Map<Integer, List<ParticipantMemState>> result = new HashMap<>();
        final List<Integer> groupSizes = groupSizeCalculator.calcGroupSizes(grouping, bids.size());

        switch (casting.getSplitPolicy()) {
            case BalancedMix:
                balancedMix(bids, result, groupSizes);
                break;
            case BestToBest:
                bestToBest(bids, result, groupSizes);
                break;
            default:
                throw internalError("Split policy " + casting.getSplitPolicy()
                        + "not implemented");
        }
        if (!bids.isEmpty()) {
            throw internalError("Left "
                    + bids.size() + " participants after group division");
        }
        result.forEach((gi, groupBids) -> log.info("Group Idx {} Uids {}", gi,
                groupBids.stream().map(o -> o.getUid().getId()).collect(toList())));
        return result;
    }

    <T> void bestToBest(List<T> bids, Map<Integer, List<T>> result, List<Integer> groups) {
        final Iterator<T> bidIterator = bids.iterator();
        for (int gi = 0; gi < groups.size(); ++gi) {
            final int groupSize = groups.get(gi);
            final List<T> groupBids = new ArrayList<>();
            for (int ibid = 0; ibid < groupSize && bidIterator.hasNext(); ++ibid) {
                groupBids.add(bidIterator.next());
                bidIterator.remove();
            }
            result.put(gi, groupBids);
        }
    }

    <T> void balancedMix(List<T> bids, Map<Integer, List<T>> result, List<Integer> groups) {
        int gi = 0;
        int delta = 1;
        final int[] allocationVector = new int[groups.size()];
        int allocations = 0;
        while (!bids.isEmpty()) {
            final Iterator<T> bidIterator = bids.iterator();
            for (; gi >= 0 && gi < groups.size(); gi += delta) {
                if (bidIterator.hasNext()) {
                    List<T> groupBids = result.get(gi);
                    if (groupBids == null) {
                        result.put(gi, groupBids = new ArrayList<>());
                    }
                    if (allocationVector[gi] < groups.get(gi)) {
                        groupBids.add(bidIterator.next());
                        bidIterator.remove();
                        ++allocationVector[gi];
                        ++allocations;
                    }
                } else {
                    return;
                }
            }
            if (allocations == 0) {
                throw internalError("Group split loops");
            }
            allocations = 0;
            delta = -delta;
            gi += delta;
        }
    }
}
