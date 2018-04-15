package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.SetsBalance;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofEntry;
import static org.dan.ping.pong.app.match.rule.service.common.WonMatchesRuleService.SUM_INT;

import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.UidsProvider;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
            UidsProvider _uids,
            GroupOrderRule rule, GroupRuleParams params) {

        final Map<Uid, Integer> uid2SetsBalance = new HashMap<>();
        final TournamentMemState tournament = params.getTournament();

        matches.get().forEach(m -> {
            final Map<Uid, Integer> uid2Sets = sports.calcWonSets(tournament, m);
            final Uid[] matchUids = m.uidsArray();
            final int balance = uid2Sets.get(matchUids[0]) - uid2Sets.get(matchUids[1]);
            uid2SetsBalance.merge(matchUids[0], balance, SUM_INT);
            uid2SetsBalance.merge(matchUids[1], -balance, SUM_INT);
        });
        return Optional.of(uid2SetsBalance.entrySet()
                .stream()
                .map((e) -> ofEntry(e, getName()))
                .sorted());
    }
}
