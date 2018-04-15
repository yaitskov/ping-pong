package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonSets;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofEntry;
import static org.dan.ping.pong.app.match.rule.service.common.WonMatchesRuleService.SUM_INT;

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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;

public class WonSetsRuleService implements GroupOrderRuleService {
    @Inject
    private Sports sports;

    @Override
    public OrderRuleName getName() {
        return WonSets;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            UidsProvider _uids,
            GroupOrderRule rule, GroupRuleParams params) {

        final Map<Uid, Integer> uid2WonSets = new HashMap<>();
        final TournamentMemState tournament = params.getTournament();

        matches.get().forEach(m -> {
            final Map<Uid, Integer> uid2Sets = sports.calcWonSets(tournament, m);
            final Uid[] matchUids = m.uidsArray();

            uid2WonSets.merge(matchUids[0], uid2Sets.get(matchUids[index(0)]), SUM_INT);
            uid2WonSets.merge(matchUids[1], uid2Sets.get(matchUids[index(1)]), SUM_INT);
        });
        return Optional.of(uid2WonSets.entrySet()
                .stream()
                .map(reasonFactory())
                .sorted());
    }

    protected Function<Map.Entry<Uid, Integer>, Reason> reasonFactory() {
        return (e) -> ofEntry(e, getName());
    }

    protected int index(int i) {
        return i;
    }
}
