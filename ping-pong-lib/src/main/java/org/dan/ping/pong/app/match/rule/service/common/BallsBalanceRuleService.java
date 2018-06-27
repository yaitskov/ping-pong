package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.BallsBalance;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BallsBalanceRuleService implements GroupOrderRuleService {
    @Override
    public OrderRuleName getName() {
        return BallsBalance;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Bid> _bids,
            GroupOrderRule _rule, GroupRuleParams _params) {
        return Optional.of(uid2BallsBalance(matches.get()).entrySet()
                .stream()
                .map((e) -> ofEntry(e, getName()))
                .sorted());
    }

    public Map<Bid, Integer> uid2BallsBalance(Stream<MatchInfo> matches) {
        final Map<Bid, Integer> bid2BallsBalance = new HashMap<>();
        matches.forEach(m -> {
            final Bid[] bids = m.bidsArray();

            m.getParticipantScore(bids[0])
                    .forEach(b -> {
                        bid2BallsBalance.merge(bids[0], b, SUM_INT);
                        bid2BallsBalance.merge(bids[1], -b, SUM_INT);
                    });
            m.getParticipantScore(bids[1])
                    .forEach(b -> {
                        bid2BallsBalance.merge(bids[1], b, SUM_INT);
                        bid2BallsBalance.merge(bids[0], -b, SUM_INT);
                    });
        });
        return bid2BallsBalance;
    }
}
