package org.dan.ping.pong.app.match.rule.service.meta;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.UseDisambiguationMatches;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class UseDisambiguationMatchesDirectiveService
        implements GroupOrderRuleService {
    @Override
    public OrderRuleName getName() {
        return UseDisambiguationMatches;
    }

    public static int matchesInGroup(int participants) {
        return participants * (participants - 1) / 2;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> disamMatchesSupplier,
            Set<Bid> bids, GroupOrderRule _rule,
            GroupRuleParams params) {
        final int expectedMatches = matchesInGroup(bids.size());
        final long actualMatches = disamMatchesSupplier.get().count();
        params.setDisambiguationMatchesWillBeCreated(actualMatches < expectedMatches);
        return Optional.empty();
    }
}
