package org.dan.ping.pong.app.match.rule.service.ping;

import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.Punkts;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofEntry;

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
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CountJustPunktsRuleService implements GroupOrderRuleService {
    public static final int WIN_POINTS = 2;
    public static final int LOST_POINTS = 1;

    @Override
    public OrderRuleName getName() {
        return Punkts;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Bid> _bids,
            GroupOrderRule _rule,
            GroupRuleParams _params) {
        final Map<Bid, Integer> uid2Points = new HashMap<>();

        matches.get().forEach(m -> m.getWinnerId().ifPresent(winBid -> {
            uid2Points.merge(winBid, WIN_POINTS, (a, b) -> a + b);
            if (m.getState() == Over) {
                uid2Points.merge(m.opponentBid(winBid),
                        LOST_POINTS, (a, b) -> a + b);
            }
        }));
        return Optional.of(uid2Points.entrySet().stream()
                .map((e) -> ofEntry(e, getName()))
                .sorted());
    }
}
