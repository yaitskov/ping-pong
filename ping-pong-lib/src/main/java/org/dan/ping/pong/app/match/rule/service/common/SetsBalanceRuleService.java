package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.SetsBalance;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofEntry;
import static org.dan.ping.pong.util.FuncUtils.SUM_INT;

import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;

@Setter
public class SetsBalanceRuleService implements GroupOrderRuleService {
    @Inject
    private Sports sports;

    @Override
    public OrderRuleName getName() {
        return SetsBalance;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Bid> _bids,
            GroupOrderRule rule, GroupRuleParams params) {

        final Map<Bid, Integer> bid2SetsBalance = new HashMap<>();
        final TournamentMemState tournament = params.getTournament();

        matches.get().forEach(m -> {
            final Map<Bid, Integer> bid2Sets = sports.calcWonSets(tournament, m);
            final Bid[] matchBids = m.bidsArray();
            final int balance = bid2Sets.get(matchBids[0]) - bid2Sets.get(matchBids[1]);
            bid2SetsBalance.merge(matchBids[0], balance, SUM_INT);
            bid2SetsBalance.merge(matchBids[1], -balance, SUM_INT);
        });
        return Optional.of(bid2SetsBalance.entrySet()
                .stream()
                .map((e) -> ofEntry(e, getName()))
                .sorted());
    }
}
