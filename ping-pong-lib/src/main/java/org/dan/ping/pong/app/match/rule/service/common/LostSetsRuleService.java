package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostSets;
import static org.dan.ping.pong.app.match.rule.reason.IncreasingIntScalarReason.ofEntry;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.Reason;

import java.util.Map;
import java.util.function.Function;

public class LostSetsRuleService extends WonSetsRuleService {
    public OrderRuleName getName() {
        return LostSets;
    }

    protected Function<Map.Entry<Uid, Integer>, Reason> reasonFactory() {
        return (e) -> ofEntry(e, LostSets);
    }

    protected int index(int i) {
        return 1 - i;
    }
}
