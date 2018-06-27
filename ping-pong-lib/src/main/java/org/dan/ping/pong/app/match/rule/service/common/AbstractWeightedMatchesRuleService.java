package org.dan.ping.pong.app.match.rule.service.common;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.group.HisIntPair;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.reason.WeightSetsReason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.util.collection.CmpValueCounter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;

public abstract class AbstractWeightedMatchesRuleService implements GroupOrderRuleService {
    @Inject
    private Sports sports;

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Bid> _bids,
            GroupOrderRule rule, GroupRuleParams params) {
        final Map<Bid, WeightSetsReason> bid2Reason = new HashMap<>();
        final TournamentMemState tournament = params.getTournament();

        matches.get().forEach(m -> {
            final Map<Bid, Integer> uid2Sets = sports.calcWonSets(tournament, m);
            merge(bid2Reason, m.bidsArray(), uid2Sets);
        });
        return Optional.of(bid2Reason.values().stream().sorted(comparatorWeightSetsReason()));
    }

    private void merge(
            Map<Bid, WeightSetsReason> bid2Reason,
            Bid[] bids,
            Map<Bid, Integer> bid2Sets) {
        for (int i = 0; i < 2; ++i) {
            WeightSetsReason reason = bid2Reason.get(bids[i]);
            if (reason == null) {
                bid2Reason.put(bids[i],
                        reason = new WeightSetsReason(
                                bids[i], getName(),
                                new TreeSet<>(comparatorCmpValueCounter())));
            }
            final CmpValueCounter<HisIntPair> zeroRepeats = new CmpValueCounter<>(
                    new HisIntPair(
                            bid2Sets.get(bids[i]),
                            bid2Sets.get(bids[1 - i])),
                    0);
            final TreeSet<CmpValueCounter<HisIntPair>> weightSets = (TreeSet) reason.getWeightSets();
            final CmpValueCounter<HisIntPair> sameScore = weightSets.lower(zeroRepeats);
            if (sameScore == null || !sameScore.getValue().equals(zeroRepeats.getValue())) {
                weightSets.add(zeroRepeats.increment());
            } else {
                weightSets.remove(sameScore);
                weightSets.add(sameScore.increment());
            }
        }
    }

    protected abstract Comparator<CmpValueCounter<HisIntPair>> comparatorCmpValueCounter();
    protected abstract Comparator<WeightSetsReason> comparatorWeightSetsReason();
}
