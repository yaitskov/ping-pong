package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Optional.empty;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.UidOrder;
import static org.dan.ping.pong.app.match.rule.reason.IncreasingIntScalarReason.ofIntI;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.util.collection.CounterInt;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class OrderUidRuleService implements GroupOrderRuleService {
    @Override
    public OrderRuleName getName() {
        return UidOrder;
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
        CounterInt c = new CounterInt();
        return Optional.of(uids.stream().sorted()
                .map(uid -> ofIntI(uid, c.postInc(), getName())));
    }
}
