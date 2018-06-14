package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Comparator.comparingInt;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpWeightedMatches;
import static org.dan.ping.pong.util.collection.IterableComparator.LengthPolicy.LongerSmaller;

import org.dan.ping.pong.app.group.HisIntPair;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.WeightSetsReason;
import org.dan.ping.pong.util.collection.CmpValueCounter;
import org.dan.ping.pong.util.collection.IterableComparator;

import java.util.Comparator;

public class AtpWeightedMatchesRuleService extends AbstractWeightedMatchesRuleService {
    private static final Comparator<CmpValueCounter<HisIntPair>> COMPARATOR
            = comparingInt(
            (CmpValueCounter<HisIntPair> cvc) ->
                    -cvc.getValue().getHis())
            .thenComparingInt((CmpValueCounter<HisIntPair> cvc) ->
                    -cvc.getValue().getEnemy())
            .thenComparingInt((CmpValueCounter<HisIntPair> cvc) ->
                    -cvc.getRepeats());

    private static final IterableComparator<CmpValueCounter<HisIntPair>> WEIGHT_SET_CMP
            = new IterableComparator<>(COMPARATOR);

    @Override
    public OrderRuleName getName() {
        return AtpWeightedMatches;
    }


    @Override
    protected Comparator<CmpValueCounter<HisIntPair>> comparatorCmpValueCounter() {
        return COMPARATOR;
    }

    private static class WeightSetsReasonComparator implements Comparator<WeightSetsReason> {
        @Override
        public int compare(WeightSetsReason o1, WeightSetsReason o2) {
            return WEIGHT_SET_CMP.compare(
                    o1.getWeightSets(), o2.getWeightSets(), LongerSmaller);
        }
    }

    private static final Comparator<WeightSetsReason> CMP = new WeightSetsReasonComparator();

    @Override
    protected Comparator<WeightSetsReason> comparatorWeightSetsReason() {
        return CMP;
    }
}
