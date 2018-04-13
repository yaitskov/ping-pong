package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.Random;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofIntD;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.UidsProvider;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.util.collection.CounterInt;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PickRandomlyRuleService implements GroupOrderRuleService {
    @Override
    public OrderRuleName getName() {
        return Random;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> _matches,
            UidsProvider uids,
            GroupOrderRule rule,
            GroupRuleParams params) {
        if (params.isDisambiguationMatchesWillBeCreated()) {
            return empty();
        }
        final CounterInt c = new CounterInt();
        final int gid = params.getGid();
        if (allUidsInOneGroup(gid)) {
            return of(uids.uids().stream()
                    .sorted(comparing(uid -> hash(gid, uid)))
                    .map(uid -> ofIntD(uid, c.postInc(), Random)));
        }
        final Map<Uid, Integer> uidGid = uids.uids().stream()
                .collect(toMap(o -> o,
                        uid -> params.getTournament().getParticipant(uid).gid()));
        return of(uids.uids().stream()
                .sorted(comparing(uid -> hash(uidGid.get(uid), uid)))
                .map(uid -> ofIntD(uid, c.postInc(), Random)));
    }

    private int hash(int gid, Uid uid) {
        return (uid.getId() * (gid + 11) % 251 << 24) + uid.getId();
    }

    private boolean allUidsInOneGroup(int gid) {
        return gid > 0;
    }
}
