package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonMatches;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofEntry;

import org.dan.ping.pong.app.bid.Uid;
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
    public static final java.util.function.BiFunction<Integer, Integer, Integer> SUM_INT = (a, b) -> a + b;

    @Override
    public OrderRuleName getName() {
        return WonMatches;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Uid> _uids,
            GroupOrderRule rule, GroupRuleParams params) {
        final Map<Uid, Integer> wons = new HashMap<>();
        matches.get().forEach(m -> {
            if (m.getWinnerId().isPresent()) {
                wons.merge(m.getWinnerId().get(), 1, SUM_INT);
                wons.merge(m.opponentUid(m.getWinnerId().get()), 0, SUM_INT);
            } else {
                m.uids().forEach(uid ->
                        wons.merge(uid, 0, (a, b) -> a + b));
            }
        });
        return Optional.of(wons.entrySet()
                .stream()
                .map((e) -> ofEntry(e, getName()))
                .sorted());
    }
}
