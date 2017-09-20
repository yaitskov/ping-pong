package org.dan.ping.pong.app.castinglots;

import static java.lang.Integer.max;
import static java.lang.Math.ceil;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.util.collection.SetUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class GroupDivider {
    public Map<Integer, List<ParticipantMemState>> divide(
            CastingLotsRule casting,
            GroupRules grouping,
            List<ParticipantMemState> bids) {
        Map<Integer, List<ParticipantMemState>> result = new HashMap<>();
        final int groups = max(1, (int) ceil(bids.size() / grouping.getMaxSize()));

        switch (casting.getSplitPolicy()) {
            case BalancedMix:
                balancedMix(bids, result, groups);
                break;
            case BestToBest:
                bestToBest(bids, result, groups);
                break;
            default:
                throw internalError("Split policy " + casting.getSplitPolicy()
                        + "not implemented");
        }
        result.forEach((gi, groupBids) -> log.info("Group Idx {} Uids {}", gi,
                groupBids.stream().map(o -> o.getUid().getId()).collect(toList())));
        return result;
    }

    private void bestToBest(List<ParticipantMemState> bids,
            Map<Integer, List<ParticipantMemState>> result, int groups) {
        final int groupSize = (int) ceil(bids.size() / groups);
        final Iterator<ParticipantMemState> bidIterator = bids.iterator();
        for (int gi = 0; gi < groups; ++gi) {
            result.put(gi, SetUtil.firstN(groupSize, bidIterator));
        }
    }

    private void balancedMix(List<ParticipantMemState> bids,
            Map<Integer, List<ParticipantMemState>> result, int groups) {
        int gi = 0;
        int delta = 1;
        int limit = 1000000;
        while (limit > 0 && !bids.isEmpty()) {
            final Iterator<ParticipantMemState> bidIterator = bids.iterator();
            for (; --limit > 0 && gi >= 0 && gi < groups; gi += delta) {
                if (bidIterator.hasNext()) {
                    List<ParticipantMemState> groupBids = result.get(gi);
                    if (groupBids == null) {
                        result.put(gi, groupBids = new ArrayList<>());
                    }
                    groupBids.add(bidIterator.next());
                    bidIterator.remove();
                } else {
                    break;
                }
            }
            delta = -delta;
            gi += delta;
        }
    }
}
