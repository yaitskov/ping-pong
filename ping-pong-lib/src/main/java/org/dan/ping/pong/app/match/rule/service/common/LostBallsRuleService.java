package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostBalls;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofEntry;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.Reason;

import java.util.Map;
import java.util.function.Function;

public class LostBallsRuleService extends WonBallsRuleService {
    public OrderRuleName getName() {
        return LostBalls;
    }

    protected Function<Map.Entry<Uid, Integer>, Reason> reasonFactory() {
        return (e) -> ofEntry(e, getName());
    }

    protected int index(int i) {
        return 1 - i;
    }
}