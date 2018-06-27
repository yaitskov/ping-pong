package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonMatches;
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

public class WonMatchesRuleService implements GroupOrderRuleService {
    @Override
    public OrderRuleName getName() {
        return WonMatches;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Bid> _bids,
            GroupOrderRule rule, GroupRuleParams params) {
        return Optional.of(findsWons(matches.get())
                .entrySet()
                .stream()
                .map((e) -> ofEntry(e, getName()))
                .sorted());
    }

    Map<Bid, Integer> findsWons(Stream<MatchInfo> matches) {
        final Map<Bid, Integer> wons = new HashMap<>();
        matches.forEach(m -> {
            if (m.getWinnerId().isPresent()) {
                wons.merge(m.getWinnerId().get(), 1, SUM_INT);
                wons.merge(m.opponentBid(m.getWinnerId().get()), 0, SUM_INT);
            } else {
                m.bids().forEach(uid ->
                        wons.merge(uid, 0, (a, b) -> a + b));
            }
        });
        return wons;
    }
}
