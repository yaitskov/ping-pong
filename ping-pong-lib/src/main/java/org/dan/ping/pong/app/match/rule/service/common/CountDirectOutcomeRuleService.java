package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.F2F;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.UidsProvider;
import org.dan.ping.pong.app.match.rule.reason.F2fReason;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CountDirectOutcomeRuleService implements GroupOrderRuleService {
    @Override
    public OrderRuleName getName() {
        return F2F;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            UidsProvider uids,
            GroupOrderRule rule, GroupRuleParams params) {
        if (uids.size() != 2) {
            return Optional.empty();
        }
        final List<MatchInfo> match = matches.get()
                .limit(2).collect(Collectors.toList());
        if (match.size() < 1) {
            return Optional.empty();
        }
        if (match.size() > 1) {
            throw internalError("To much matches");
        }
        final MatchInfo m = match.get(0);
        return m.getWinnerId()
                .map(wid -> {
                    final Uid opponentUid = m.opponentUid(wid);
                    return Stream.of(
                            F2fReason.builder()
                                    .uid(wid)
                                    .won(1)
                                    .opponentUid(opponentUid)
                                    .build(),
                            F2fReason.builder()
                                    .uid(opponentUid)
                                    .opponentUid(wid)
                                    .build());
                });
    }
}
