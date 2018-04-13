package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonBalls;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofEntry;
import static org.dan.ping.pong.app.match.rule.service.common.CountWonMatchesRuleService.SUM_INT;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.UidsProvider;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
            UidsProvider _uids,
            GroupOrderRule rule, GroupRuleParams params) {
        final Map<Uid, Integer> uid2WonBalls = new HashMap<>();
        matches.get().forEach(m -> {
            final Uid[] uids = m.uidsArray();
            m.getParticipantScore(uids[0])
                    .forEach(b -> uid2WonBalls.merge(uids[index(0)], b, SUM_INT));
            m.getParticipantScore(uids[1])
                    .forEach(b -> uid2WonBalls.merge(uids[index(1)], b, SUM_INT));
        });

        return Optional.of(uid2WonBalls.entrySet()
                .stream()
                .map(reasonFactory())
                .sorted());
    }

    protected Function<Map.Entry<Uid, Integer>, Reason> reasonFactory() {
        return (e) -> ofEntry(e, WonBalls);
    }

    protected int index(int i) {
        return i;
    }
}
