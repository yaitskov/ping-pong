package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostBalls;
import static org.dan.ping.pong.app.match.rule.reason.IncreasingIntScalarReason.ofEntry;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.Reason;

import java.util.Map;
import java.util.function.Function;

public class LostBallsRuleService extends WonBallsRuleService {
    public OrderRuleName getName() {
        return LostBalls;
    }

    protected Function<Map.Entry<Bid, Integer>, Reason> reasonFactory() {
        return (e) -> ofEntry(e, getName());
    }

    protected int index(int i) {
        return 1 - i;
    }
}
