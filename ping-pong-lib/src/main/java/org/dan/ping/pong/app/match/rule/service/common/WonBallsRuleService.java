package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonBalls;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofEntry;
import static org.dan.ping.pong.util.FuncUtils.SUM_INT;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class WonBallsRuleService implements GroupOrderRuleService {
    @Override
    public OrderRuleName getName() {
        return WonBalls;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Bid> _bids,
            GroupOrderRule _rule, GroupRuleParams _params) {
        final Map<Bid, Integer> bid2WonBalls = new HashMap<>();
        matches.get().forEach(m -> {
            final Bid[] bids = m.bidsArray();
            m.getParticipantScore(bids[0])
                    .forEach(b -> bid2WonBalls.merge(bids[index(0)], b, SUM_INT));
            m.getParticipantScore(bids[1])
                    .forEach(b -> bid2WonBalls.merge(bids[index(1)], b, SUM_INT));
        });

        return Optional.of(bid2WonBalls.entrySet()
                .stream()
                .map(reasonFactory())
                .sorted());
    }

    protected Function<Map.Entry<Bid, Integer>, Reason> reasonFactory() {
        return (e) -> ofEntry(e, getName());
    }

    protected int index(int i) {
        return i;
    }
}
