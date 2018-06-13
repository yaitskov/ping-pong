package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.BallsBalance;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofEntry;
import static org.dan.ping.pong.util.FuncUtils.SUM_INT;

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

public class BallsBalanceRuleService implements GroupOrderRuleService {
    @Override
    public OrderRuleName getName() {
        return BallsBalance;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Uid> _uids,
            GroupOrderRule _rule, GroupRuleParams _params) {
        return Optional.of(uid2BallsBalance(matches.get()).entrySet()
                .stream()
                .map((e) -> ofEntry(e, getName()))
                .sorted());
    }

    public Map<Uid, Integer> uid2BallsBalance(Stream<MatchInfo> matches) {
        final Map<Uid, Integer> uid2BallsBalance = new HashMap<>();
        matches.forEach(m -> {
            final Uid[] uids = m.uidsArray();

            m.getParticipantScore(uids[0])
                    .forEach(b -> {
                        uid2BallsBalance.merge(uids[0], b, SUM_INT);
                        uid2BallsBalance.merge(uids[1], -b, SUM_INT);
                    });
            m.getParticipantScore(uids[1])
                    .forEach(b -> {
                        uid2BallsBalance.merge(uids[1], b, SUM_INT);
                        uid2BallsBalance.merge(uids[0], -b, SUM_INT);
                    });
        });
        return uid2BallsBalance;
    }
}
