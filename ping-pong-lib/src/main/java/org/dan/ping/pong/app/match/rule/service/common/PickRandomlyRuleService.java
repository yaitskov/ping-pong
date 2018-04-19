package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.Random;
import static org.dan.ping.pong.app.match.rule.reason.IncreasingIntScalarReason.ofIntI;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.util.collection.CounterInt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PickRandomlyRuleService implements GroupOrderRuleService {
    @Override
    public OrderRuleName getName() {
        return Random;
    }

    private List<Uid> shufflePredicted(Collection<Uid> uids, int gid) {
        final List<Uid> result = new ArrayList<>(uids);
        Collections.sort(result);
        Collections.shuffle(result, new Random(gid));
        return result;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> _matches,
            Set<Uid> uids,
            GroupOrderRule rule,
            GroupRuleParams params) {
        if (params.isDisambiguationMatchesWillBeCreated()) {
            return empty();
        }
        final int gid = params.getGid();
        if (allUidsInOneGroup(gid)) {
            final CounterInt c = new CounterInt();
            return of(shufflePredicted(uids, gid).stream()
                    .map(uid -> ofIntI(uid, c.postInc(), getName())));
        }
        final Multimap<Integer, Uid> gidUids = HashMultimap.create();
        uids.forEach(uid -> gidUids.put(
                params.getTournament().getParticipant(uid).gid(), uid));
        final List<Integer> gids = new ArrayList<>(gidUids.keySet());
        Collections.sort(gids);
        Collections.shuffle(gids, new Random(gids.stream().mapToInt(o -> o).sum()));

        final Map<Integer, List<Uid>> gidOrderedUids = new HashMap<>();
        gids.forEach(g -> gidOrderedUids.put(g, shufflePredicted(gidUids.get(g), g)));
        final List<Uid> allUids = new ArrayList<>();
        final List<Integer> toBeRemoved = new ArrayList<>();
        while (gids.size() > 0) {
            for (int i = 0; i < gids.size(); ++i) {
                List<Uid> uidsOfGid = gidOrderedUids.get(gids.get(i));
                if (uidsOfGid.isEmpty()) {
                    toBeRemoved.add(i);
                    continue;
                }
                allUids.add(uidsOfGid.get(0));
                uidsOfGid.remove(0);
            }
            for (int i = toBeRemoved.size() - 1; i >= 0; --i) {
                gids.remove((int) toBeRemoved.get(i));
            }
            toBeRemoved.clear();
        }
        final CounterInt c = new CounterInt();
        return of(allUids.stream()
                .map(uid -> ofIntI(uid, c.postInc(), getName())));
    }

    private boolean allUidsInOneGroup(int gid) {
        return gid > 0;
    }
}
