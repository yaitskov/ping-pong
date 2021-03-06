package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.F2F;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.F2fReason;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectOutcomeRuleService implements GroupOrderRuleService {
    @Override
    public OrderRuleName getName() {
        return F2F;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Bid> bids,
            GroupOrderRule rule, GroupRuleParams params) {
        if (bids.size() != 2) {
            return Optional.empty();
        }
        final List<MatchInfo> match = matches.get()
                .limit(2).collect(Collectors.toList());
        if (match.size() < 1) {
            return Optional.empty(); // could be different groups
        }
        if (match.size() > 1) {
            throw internalError("To much matches");
        }
        final MatchInfo m = match.get(0);
        return m.getWinnerId()
                .map(wid -> {
                    final Bid opponentBid = m.opponentBid(wid);
                    checkMatchUid(bids, m, wid);
                    checkMatchUid(bids, m, opponentBid);
                    return Stream.of(
                            F2fReason.builder()
                                    .bid(wid)
                                    .won(1)
                                    .opponentBid(opponentBid)
                                    .build(),
                            F2fReason.builder()
                                    .bid(opponentBid)
                                    .opponentBid(wid)
                                    .build());
                });
    }

    private void checkMatchUid(Set<Bid> bids, MatchInfo m, Bid wid) {
        if (!bids.contains(wid)) {
            throw internalError("F2f rule uids mismatch "
                    + wid + " in mid " + m.getMid());
        }
    }
}
