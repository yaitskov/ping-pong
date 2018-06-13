package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.WeightedMatches;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.HisIntPair;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.reason.WeightSetsReason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.util.collection.CmpValueCounter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;

public class WeightedMatchesRuleService implements GroupOrderRuleService {
    @Inject
    private Sports sports;

    @Override
    public OrderRuleName getName() {
        return WeightedMatches;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Uid> _uids,
            GroupOrderRule rule, GroupRuleParams params) {
        final Map<Uid, WeightSetsReason> uid2Reason = new HashMap<>();
        final TournamentMemState tournament = params.getTournament();

        matches.get().forEach(m -> {
            final Map<Uid, Integer> uid2Sets = sports.calcWonSets(tournament, m);
            merge(uid2Reason, m.uidsArray(), uid2Sets);
        });
        return Optional.of(uid2Reason.values().stream().sorted());
    }

    private void merge(
            Map<Uid, WeightSetsReason> uid2Reason,
            Uid[] uids,
            Map<Uid, Integer> uid2Sets) {
        for (int i = 0; i < 2; ++i) {
            WeightSetsReason reason = uid2Reason.get(uids[i]);
            if (reason == null) {
                uid2Reason.put(uids[i],
                        reason = new WeightSetsReason(uids[i], getName(), new TreeSet<>()));
            }
            final CmpValueCounter<HisIntPair> zeroRepeats = new CmpValueCounter<>(
                    new HisIntPair(
                            uid2Sets.get(uids[i]),
                            uid2Sets.get(uids[1 - i])),
                    0);
            final TreeSet<CmpValueCounter<HisIntPair>> weightSets = reason.getWeightSets();
            final CmpValueCounter<HisIntPair> sameScore = weightSets.lower(zeroRepeats);
            if (sameScore == null || !sameScore.getValue().equals(zeroRepeats.getValue())) {
                weightSets.add(zeroRepeats.increment());
            } else {
                weightSets.remove(sameScore);
                weightSets.add(sameScore.increment());
            }
        }
    }
}
